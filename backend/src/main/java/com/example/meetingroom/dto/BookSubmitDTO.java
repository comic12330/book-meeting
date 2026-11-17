package com.example.meetingroom.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class BookSubmitDTO {

    /** 会议室 */
    @NotNull(message = "请选择会议室")
    private Long roomId;

    /** 开始时间 */
    @NotNull(message = "请选择开始时间")
    private LocalDateTime startTime;

    /** 结束时间 */
    @NotNull(message = "请选择结束时间")
    private LocalDateTime endTime;

    /** 会议用途 */
    @NotBlank(message = "请填写会议用途")
    private String purpose;

    /** 参会人数 */
    @NotNull(message = "请填写参会人数")
    @Min(value = 1, message = "参会人数至少为 1")
    private Integer participantNum;

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
}
