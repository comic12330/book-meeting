package com.example.meetingroom.entity;

import java.time.LocalDateTime;

public class MeetingBook {

    /** 主键 */
    private Long id;

    /** 申请人 */
    private Long userId;

    /** 会议室 */
    private Long roomId;

    /** 使用开始时间 */
    private LocalDateTime startTime;

    /** 使用结束时间 */
    private LocalDateTime endTime;

    /** 会议用途 */
    private String purpose;

    /** 参会人数 */
    private Integer participantNum;

    /** 0 待审核，1 通过，2 驳回，3 取消，4 已完成 */
    private Integer status;

    /** 审核意见或驳回理由 */
    private String auditMsg;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 提交时间 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getParticipantNum() {
        return participantNum;
    }

    public void setParticipantNum(Integer participantNum) {
        this.participantNum = participantNum;
    }

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

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
