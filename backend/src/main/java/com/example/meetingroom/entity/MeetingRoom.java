package com.example.meetingroom.entity;

import java.time.LocalDateTime;

/**
 * 会议室，对应 meeting_room 表，字段与表列一一对应。
 */
public class MeetingRoom {

    /** 主键 */
    private Long id;

    /** 会议室名称 */
    private String roomName;

    /** 容纳人数 */
    private Integer capacity;

    /** 所在位置 */
    private String location;

    /** 可用设备，英文逗号分隔 */
    private String equipment;

    /** 0 停用，1 启用 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
