package com.example.meetingroom.dto;

import com.example.meetingroom.common.PasswordPolicy;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员重置他人密码。
 * 不校验原密码——执行者已通过 /api/admin/** 的管理员身份校验，验证方式与管理端其他接口一致。
 */
public class ResetPasswordDTO {

    /** 管理员指定的新密码 */
    @NotBlank(message = "请输入新密码")
    @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH,
            message = "密码长度需在 6-32 位之间")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
