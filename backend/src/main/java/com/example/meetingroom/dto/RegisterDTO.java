package com.example.meetingroom.dto;

import com.example.meetingroom.common.PasswordPolicy;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 自助注册的表单，只接收账号、密码、姓名与电话，角色和状态由服务端固定。
 */
public class RegisterDTO {

    /** 登录账号 */
    @NotBlank(message = "请输入账号")
    @Size(min = 3, max = 20, message = "账号长度需在 3-20 位之间")
    private String username;

    /** 登录密码 */
    @NotBlank(message = "请输入密码")
    @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH,
            message = "密码长度需在 6-32 位之间")
    private String password;

    /** 真实姓名 */
    @NotBlank(message = "请输入真实姓名")
    private String realName;

    /** 联系电话 */
    private String phone;

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
}
