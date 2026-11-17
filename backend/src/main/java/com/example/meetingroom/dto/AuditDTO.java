package com.example.meetingroom.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AuditDTO {

    /** 1 通过，2 驳回 */
    @NotNull(message = "请选择审核结果")
    private Integer status;

    /** 审核意见；驳回时必填 */
    private String auditMsg;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAuditMsg() {
        return auditMsg;
    }

    public void setAuditMsg(String auditMsg) {
        this.auditMsg = auditMsg;
    }
}
