package com.example.meetingroom.common;

/**
 * 业务异常：由全局异常处理器统一转成 Result 返回，不暴露堆栈给前端。
 */
public class BusinessException extends RuntimeException {

    /** 业务状态码，会原样放进 Result.code */
    private final Integer code;

    public BusinessException(String message) {
        this(500, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
