package com.example.meetingroom.dto;

import com.example.meetingroom.common.PasswordPolicy;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 管理端新增 / 编辑用户表单。
 * 编辑时不解析 password 字段（密码只能走「重置密码」接口），因此这里不做非空约束。
 */
public class UserSaveDTO {

    /** 登录账号，仅新增时使用；编辑时不允许修改 */
    @NotBlank(message = "请输入登录账号")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "账号为 3-20 位字母、数字或下划线")
    private String username;

    /** 初始密码，仅新增时使用 */
    @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH,
            message = "密码长度需在 6-32 位之间")
    private String password;

    /** 真实姓名 */
    @NotBlank(message = "请输入真实姓名")
    @Size(max = 50, message = "真实姓名不超过 50 个字符")
    private String realName;

    /** 联系电话，允许为空 */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 0 普通用户，1 管理员 */
    @NotNull(message = "请选择角色")
    private Integer role;

    /** 0 禁用，1 可用 */
    @NotNull(message = "请选择账号状态")
    private Integer status;

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
}
