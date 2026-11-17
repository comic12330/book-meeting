package com.example.meetingroom.entity;

import java.time.LocalDateTime;

public class User {

    /** 主键 */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码摘要，不返回给前端 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 联系电话 */
    private String phone;

    /** 0 普通用户，1 管理员 */
    private Integer role;

    /** 0 禁用，1 可用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
