package com.example.meetingroom.common;

/**
 * 统一返回结果。
 * 约定：HTTP 状态码统一 200，业务状态放在 code 字段，前端按 code 判断。
 */
public class Result<T> {

    /** 200 成功；400 参数错误；401 未登录；403 无权限；500 系统异常；1001 业务失败 */
    private Integer code;

    /** 提示语，前端直接展示给用户 */
    private String msg;

    /** 业务数据，失败时为 null */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /** 成功，不带数据 */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /** 成功，带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 成功，使用自定义提示语 */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /** 失败，指定业务码与提示语 */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /** 失败，业务码固定为 500 */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
