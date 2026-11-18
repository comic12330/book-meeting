package com.example.meetingroom.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员重置他人密码。
 * 不校验原密码——执行者已通过 /api/admin/** 的管理员身份校验，验证方式与管理端其他接口一致。
 */
public class ResetPasswordDTO {

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
