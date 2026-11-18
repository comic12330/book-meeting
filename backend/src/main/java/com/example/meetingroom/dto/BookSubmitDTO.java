package com.example.meetingroom.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 提交预定表单。
 * <p>
 * 这里的注解只做「必填、最小值」这类单字段校验，由 Spring 自动触发。
 * 涉及字段之间关系的规则一概不在这里判断，而是放在 BookService 里：
 * 结束时间是否晚于开始时间、开始时间是否已过去、单次时长是否超上限、时段是否冲突。
 * 原因有两个：单字段注解表达不了区间关系；且前端可以绕过，真正的校验必须在服务层。
 * <p>
 * 时间字段用 LocalDateTime，天然支持跨天（例如 23:00 到次日 02:00），不做「同一天内」的限制。
 */
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
