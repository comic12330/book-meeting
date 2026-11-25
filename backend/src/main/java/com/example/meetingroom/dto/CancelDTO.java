package com.example.meetingroom.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员取消他人预定的表单。
 *
 * 理由必填：这个动作会单方面取消别人已经安排好的会议，当事人必须能从「我的预定」里看到为什么被取消。与驳回必填理由同理，且影响面比驳回更大。
 */
public class CancelDTO {

    /** 取消理由，会展示给被取消预定的人 */
    @NotBlank(message = "请填写取消理由")
    @Size(max = 255, message = "取消理由不能超过 255 字")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
