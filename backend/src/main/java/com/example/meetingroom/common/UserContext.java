package com.example.meetingroom.common;

/**
 * 当前登录用户上下文（线程级），由登录拦截器写入、请求结束时清理。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    /** 请求进入时写入，用户信息来自令牌解析，后续业务代码从这里取当前用户 */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /** 取完整登录信息，未登录时为 null */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 取当前用户 ID，业务层用它限定数据范围，避免从前端接收 ID */
    public static Long userId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    /** 判断当前用户是否管理员，用于服务层做数据归属校验 */
    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && UserRole.isAdmin(user.getRole());
    }

    /**
     * 请求结束时清理。
     * ThreadLocal 绑在线程上，Tomcat 的工作线程会被复用，
     * 不清理会让下一个请求读到上一个用户的身份，造成越权。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
