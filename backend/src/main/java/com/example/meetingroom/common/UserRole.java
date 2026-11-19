package com.example.meetingroom.common;

/**
 * 用户角色与账号状态字典，对应 user.role 与 user.status。
 */
public final class UserRole {

    /** 普通用户 */
    public static final int NORMAL = 0;

    /** 系统管理员 */
    public static final int ADMIN = 1;

    /** 账号可用 */
    public static final int STATUS_ENABLED = 1;

    /** 账号停用 */
    public static final int STATUS_DISABLED = 0;

    private UserRole() {
    }

    public static boolean isAdmin(Integer role) {
        return Integer.valueOf(ADMIN).equals(role);
    }

    public static boolean isEnabled(Integer status) {
        return Integer.valueOf(STATUS_ENABLED).equals(status);
    }
}
