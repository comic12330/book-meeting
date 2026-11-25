package com.example.meetingroom.common;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;

/**
 * 全局异常处理：所有异常都转成统一返回体，前端拿不到原始堆栈。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：按异常自带的业务码与提示语返回，前端可直接展示给用户 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常：code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 表单字段校验失败：取第一条字段错误的信息返回 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(Exception e) {
        BindingResult bindingResult = e instanceof MethodArgumentNotValidException
                ? ((MethodArgumentNotValidException) e).getBindingResult()
                : ((BindException) e).getBindingResult();
        FieldError fieldError = bindingResult.getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return Result.error(400, msg);
    }

    /** 方法参数上的约束校验失败，例如 @NotBlank 直接标在 Controller 参数上 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraint(ConstraintViolationException e) {
        return Result.error(400, e.getMessage());
    }

    /** 请求体解析失败：时间字段格式错误时给出明确提示，其余情况只给通用提示 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        Throwable cause = e.getCause();
        // 只有确实是时间字段解析失败才提示时间格式，其余情形（空请求体、非法字符、JSON 结构错误）一律给通用提示，避免把排查方向带偏。
        if (cause instanceof InvalidFormatException
                && LocalDateTime.class.isAssignableFrom(((InvalidFormatException) cause).getTargetType())) {
            return Result.error(400, "时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
        return Result.error(400, "请求内容格式不正确，请检查提交的数据");
    }

    /** 违反数据库约束，例如删除仍被其他记录引用的数据 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("数据完整性异常：{}", e.getMessage());
        return Result.error(400, "该数据被其他记录引用，无法完成当前操作");
    }

    /** 未预料的异常：记录完整堆栈供排查，只向前端返回通用提示 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
