package com.example.meetingroom.common;

/**
 * 从令牌解析出来的登录用户快照。
 */
public class LoginUser {

    /** 用户 ID，服务端授权与数据归属判断都以它为准 */
    private final Long userId;

    /** 登录账号，只用于展示 */
    private final String username;

    /** 角色：0 普通用户，1 管理员 */
    private final Integer role;

    /**
     * 真实姓名。
     * 不进令牌载荷：令牌只放标识与角色，姓名这类会变的资料每次登录后由拦截器回查数据库补上，放进载荷反而会出现「改了姓名但令牌里的旧姓名还能用到过期」。
     */
    private final String realName;

    public LoginUser(Long userId, String username, Integer role) {
        this(userId, username, role, null);
    }

    public LoginUser(Long userId, String username, Integer role, String realName) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.realName = realName;
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

    public String getRealName() {
        return realName;
    }
}
