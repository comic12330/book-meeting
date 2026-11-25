package com.example.meetingroom.vo;

import java.time.LocalDateTime;

/**
 * 预定的列表与详情返回对象，在实体字段之外附带会议室名称、位置与申请人姓名，前端不必再逐个查。
 */
public class BookVO {

    /** 预定单 ID */
    private Long id;

    /** 申请人 ID */
    private Long userId;

    /** 会议室 ID */
    private Long roomId;

    /** 会议室名称 */
    private String roomName;

    /** 会议室位置 */
    private String location;

    /** 申请人账号 */
    private String username;

    /** 申请人姓名 */
    private String realName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 会议用途 */
    private String purpose;

    /** 参会人数 */
    private Integer participantNum;

    /** 0 待审核，1 通过，2 驳回，3 取消，4 已完成 */
    private Integer status;

    /** 审核意见 */
    private String auditMsg;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 取消理由 */
    private String cancelReason;

    /** 取消时间 */
    private LocalDateTime cancelTime;

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

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

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

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
