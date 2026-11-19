package com.example.meetingroom.common;

/**
 * 会议室状态字典，对应 meeting_room.status。
 */
public final class RoomStatus {

    /** 停用，不参与预定 */
    public static final int DISABLED = 0;

    /** 启用 */
    public static final int ENABLED = 1;

    private RoomStatus() {
    }

    public static boolean isEnabled(Integer status) {
        return Integer.valueOf(ENABLED).equals(status);
    }
}
