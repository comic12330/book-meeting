package com.example.meetingroom.mapper;

import com.example.meetingroom.dto.RoomQueryDTO;
import com.example.meetingroom.entity.MeetingRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MeetingRoomMapper {

    /** 条件查询，条件为空时返回全部；status 是否限定由调用方决定 */
    List<MeetingRoom> selectByCondition(RoomQueryDTO query);

    /** 按主键取会议室，不存在时由服务层抛业务异常 */
    MeetingRoom findById(@Param("id") Long id);

    /**
     * 对会议室行加排他锁（SELECT ... FOR UPDATE）。
     * 审核时先用它把「同一会议室」的审核串行化，避免两个管理员同时通过两条重叠申请。
     * 必须运行在事务中，否则锁在语句结束后立即释放。
     */
    Long lockById(@Param("id") Long id);

    /** 新增会议室，回填自增主键 */
    int insert(MeetingRoom room);

    /** 整条覆盖更新 */
    int update(MeetingRoom room);

    /** 物理删除，调用前必须确认该会议室没有预定引用 */
    int deleteById(@Param("id") Long id);
}
