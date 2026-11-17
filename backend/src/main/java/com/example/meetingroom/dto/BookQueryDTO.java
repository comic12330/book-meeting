package com.example.meetingroom.dto;

import com.example.meetingroom.common.PageQuery;

public class BookQueryDTO extends PageQuery {

    /** 预定状态，不传表示全部 */
    private Integer status;

    /** 会议室，不传表示全部 */
    private Long roomId;

    /** 申请人或用途关键字 */
    private String keyword;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
