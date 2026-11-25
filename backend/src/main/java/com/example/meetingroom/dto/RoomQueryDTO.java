package com.example.meetingroom.dto;

import com.example.meetingroom.common.PageQuery;

/**
 * 会议室列表的查询条件，分页参数来自父类 PageQuery。
 */
public class RoomQueryDTO extends PageQuery {

    /** 名称模糊查询 */
    private String roomName;

    /** 位置模糊查询 */
    private String location;

    /** 最少容纳人数 */
    private Integer minCapacity;

    /** 必须包含的设备编码 */
    private String equipment;

    /** 0 停用，1 启用；不传表示全部（仅管理端用） */
    private Integer status;

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

    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
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
}
