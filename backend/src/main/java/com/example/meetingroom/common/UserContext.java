package com.example.meetingroom.common;

/**
 * 当前登录用户上下文（线程级），由登录拦截器写入、请求结束时清理。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long userId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && Integer.valueOf(1).equals(user.getRole());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
