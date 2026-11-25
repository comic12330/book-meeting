package com.example.meetingroom.dto;

import com.example.meetingroom.common.PasswordPolicy;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 修改本人密码的表单：原密码与新密码。
 */
public class PasswordDTO {

    /** 原密码 */
    @NotBlank(message = "请输入原密码")
    private String oldPassword;

    /** 新密码 */
    @NotBlank(message = "请输入新密码")
    @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH,
            message = "密码长度需在 6-32 位之间")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
