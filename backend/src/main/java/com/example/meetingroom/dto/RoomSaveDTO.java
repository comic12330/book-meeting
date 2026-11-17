package com.example.meetingroom.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class RoomSaveDTO {

    /** 会议室名称 */
    @NotBlank(message = "请输入会议室名称")
    private String roomName;

    /** 容纳人数 */
    @NotNull(message = "请输入容纳人数")
    @Min(value = 1, message = "容纳人数至少为 1")
    private Integer capacity;

    /** 所在位置 */
    @NotBlank(message = "请输入所在位置")
    private String location;

    /** 可用设备，英文逗号分隔 */
    private String equipment;

    /** 0 停用，1 启用 */
    private Integer status;

    /** 备注 */
    private String remark;

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
}
