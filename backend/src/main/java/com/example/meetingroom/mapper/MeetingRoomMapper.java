package com.example.meetingroom.mapper;

import com.example.meetingroom.dto.RoomQueryDTO;
import com.example.meetingroom.entity.MeetingRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MeetingRoomMapper {

    List<MeetingRoom> selectByCondition(RoomQueryDTO query);

    MeetingRoom findById(@Param("id") Long id);

    /**
     * 对会议室行加排他锁（SELECT ... FOR UPDATE）。
     * 审核时先用它把「同一会议室」的审核串行化，避免两个管理员同时通过两条重叠申请。
     * 必须运行在事务中，否则锁在语句结束后立即释放。
     */
    Long lockById(@Param("id") Long id);

    int insert(MeetingRoom room);

    int update(MeetingRoom room);

    int deleteById(@Param("id") Long id);
}
