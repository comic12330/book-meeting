package com.example.meetingroom.dto;

import com.example.meetingroom.common.PageQuery;

/**
 * 管理端用户列表查询条件。
 */
public class UserQueryDTO extends PageQuery {

    /** 登录账号，模糊匹配 */
    private String username;

    /** 真实姓名，模糊匹配 */
    private String realName;

    /** 0 普通用户，1 管理员；不传表示全部 */
    private Integer role;

    /** 0 禁用，1 可用；不传表示全部 */
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
