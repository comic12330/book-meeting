package com.example.meetingroom.common;

/**
 * 从令牌解析出来的登录用户快照。
 */
public class LoginUser {

    private final Long userId;

    private final String username;

    private final Integer role;

    public LoginUser(Long userId, String username, Integer role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Integer getRole() {
        return role;
    }
}
