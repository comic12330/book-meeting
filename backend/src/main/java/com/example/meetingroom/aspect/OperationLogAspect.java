package com.example.meetingroom.aspect;

import com.example.meetingroom.annotation.LogOperation;
import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.LoginUser;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.entity.OperationLog;
import com.example.meetingroom.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面：把带 {@link LogOperation} 的方法调用记入 operation_log。
 *
 * 操作人在主线程快照：{@link UserContext} 是 ThreadLocal，一旦把记录动作丢给异步线程就读不到，
 * 所以进入方法时先取出用户存进局部变量，后面无论同步写还是事务回调里写都不会丢。
 * 成功日志在事务提交后写：若与业务同事务，业务回滚时日志会一起消失；若强行独立事务先提交，
 * 又会出现「日志说有、业务表没有」。注册 afterCommit 回调，业务真正落库了才记，两者保持一致。
 * 失败日志立即写：此时外层事务尚未回滚，{@link OperationLogService#record} 用 REQUIRES_NEW
 * 挂起它、独立提交，业务回滚后这条记录仍在。
 * 记录失败不影响业务：日志是旁路，写不进去只打应用日志告警。
 * 失败记录只记「业务拒绝」类原因（如时段冲突、不能停用自己），参数格式错误这类请求
 * 在进入方法前就被拦下，走不到本切面，不会产生噪声。
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    /** 与表结构一致，避免超长串把插入打挂 */
    private static final int MAX_MSG_LENGTH = 255;

    private static final int MAX_TARGET_LENGTH = 200;

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Resource
    private OperationLogService logService;

    @Around("@annotation(annotation)")
    public Object around(ProceedingJoinPoint pjp, LogOperation annotation) throws Throwable {
        LoginUser user = UserContext.get();
        // 理论上走不到：管理端接口都经过了 AuthInterceptor。真走到这里说明注解贴错了方法，
        // 跳过记录但放行执行，不能因为日志把业务拦住。
        if (user == null) {
            return pjp.proceed();
        }

        String target = resolveTarget(annotation.target(), pjp);

        Object result;
        try {
            result = pjp.proceed();
        } catch (Throwable e) {
            writeQuietly(buildLog(user, annotation, target, 0, failureReason(e)));
            throw e;
        }
        writeAfterCommit(buildLog(user, annotation, target, 1, null));
        return result;
    }

    // ------------------------------------------------------------ 私有方法

    private OperationLog buildLog(LoginUser user, LogOperation annotation, String target,
                                  int result, String resultMsg) {
        OperationLog entity = new OperationLog();
        entity.setOperatorId(user.getUserId());
        // 存姓名快照，姓名缺失（理论上不会）时退回账号，避免日志里出现空白操作人
        entity.setOperatorName(user.getRealName() == null ? user.getUsername() : user.getRealName());
        entity.setModule(annotation.type().getModule().name());
        entity.setOpType(annotation.type().name());
        entity.setOpResult(result);
        entity.setResultMsg(resultMsg);
        entity.setTargetDesc(target);
        entity.setOpTime(LocalDateTime.now());
        return entity;
    }

    /**
     * 成功日志：事务活跃时挂到提交之后，没有事务（例如手动触发的扫描接口）则直接写。
     * 两种情况都安全，因此本切面没有刻意调整与事务拦截器的先后顺序。
     */
    private void writeAfterCommit(OperationLog entity) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    writeQuietly(entity);
                }
            });
        } else {
            writeQuietly(entity);
        }
    }

    private void writeQuietly(OperationLog entity) {
        try {
            logService.record(entity);
        } catch (Exception e) {
            log.warn("操作日志写入失败，不影响业务：type={}, operator={}",
                    entity.getOpType(), entity.getOperatorId(), e);
        }
    }

    /**
     * SpEL 解析操作对象描述，取不到就留空，不抛出去。
     * 上下文把被代理对象设为根对象，因此表达式里除了取参数（{@code #dto.roomName}），
     * 还能调用业务对象的公开方法，删除、取消这类只拿到主键的方法靠它换出可读名称
     * （如 {@code get(#id).roomName}）。这里多一次按主键取数，只发生在低频的管理操作上。
     * 参数名依赖编译时的 {@code -parameters}（Spring Boot parent 默认开启），
     * 若改成不带参数名编译，这里会解析失败、描述留空，但日志本身照常写入。
     */
    private String resolveTarget(String expression, ProceedingJoinPoint pjp) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        try {
            Method method = ((MethodSignature) pjp.getSignature()).getMethod();
            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                    pjp.getTarget(), method, pjp.getArgs(), NAME_DISCOVERER);
            Object value = PARSER.parseExpression(expression).getValue(context);
            if (value == null) {
                return null;
            }
            String text = String.valueOf(value);
            return text.length() > MAX_TARGET_LENGTH ? text.substring(0, MAX_TARGET_LENGTH) : text;
        } catch (Exception e) {
            log.warn("操作日志的对象描述解析失败，表达式：{}", expression, e);
            return null;
        }
    }

    /**
     * 失败原因。业务异常取原始提示即可（面向使用者的文案）；其余异常补上类型，
     * 否则只留一句 SQL 报错很难定位是哪一步炸的。
     */
    private String failureReason(Throwable e) {
        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = e.getClass().getSimpleName();
        } else if (!(e instanceof BusinessException)) {
            message = e.getClass().getSimpleName() + ": " + message;
        }
        return message.length() > MAX_MSG_LENGTH ? message.substring(0, MAX_MSG_LENGTH) : message;
    }
}
