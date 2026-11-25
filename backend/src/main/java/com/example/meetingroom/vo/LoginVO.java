package com.example.meetingroom.vo;

/**
 * 登录成功后返回的令牌与用户基本信息，前端保存令牌并在后续请求中携带。
 */
public class LoginVO {

    /** 访问令牌 */
    private String token;

    /** 用户 ID */
    private Long userId;

    /** 登录账号 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 0 普通用户，1 管理员 */
    private Integer role;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
}
