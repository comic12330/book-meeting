package com.example.meetingroom.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 登录表单：账号与密码。
 */
public class LoginDTO {

    /** 登录账号 */
    @NotBlank(message = "请输入账号")
    private String username;

    /** 登录密码 */
    @NotBlank(message = "请输入密码")
    private String password;

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
}
