package com.example.meetingroom.annotation;

import com.example.meetingroom.common.OperationType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个需要记入操作日志的方法。
 *
 * 贴在 Service 的写方法上，而不是 Controller：注解与业务代码放在一起，改业务时不会漏掉「这里也是一个管理动作」。切面见{@link com.example.meetingroom.aspect.OperationLogAspect}。
 * 只贴在管理端入口最终调用的方法上。普通用户的提交、取消、改资料不加此注解，本功能的目标是审计管理员的数据变更，不是分析用户行为。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /** 操作类型，决定日志归入哪个模块、显示成什么名称 */
    OperationType type();

    /**
     * 操作对象的可读描述，支持 SpEL，可留空。
     * 例：{@code target = "#dto.roomName"}。解析失败或表达式为空时该字段留空，不影响日志写入，描述取不到不应导致整条日志丢弃。
     */
    String target() default "";
}
