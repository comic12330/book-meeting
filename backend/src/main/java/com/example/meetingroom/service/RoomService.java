package com.example.meetingroom.service;

import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.dto.RoomQueryDTO;
import com.example.meetingroom.dto.RoomSaveDTO;
import com.example.meetingroom.entity.MeetingRoom;
import com.example.meetingroom.mapper.MeetingBookMapper;
import com.example.meetingroom.mapper.MeetingRoomMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 会议室资源管理。
 */
@Service
public class RoomService {

    @Resource
    private MeetingRoomMapper roomMapper;

    @Resource
    private MeetingBookMapper bookMapper;

    /** 用户端：只能看到启用中的会议室 */
    public PageResult<MeetingRoom> pageForUser(RoomQueryDTO query) {
        query.setStatus(1);
        return doPage(query);
    }

    /** 管理端：可查看全部状态 */
    public PageResult<MeetingRoom> pageForAdmin(RoomQueryDTO query) {
        return doPage(query);
    }

    private PageResult<MeetingRoom> doPage(RoomQueryDTO query) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<MeetingRoom> list = roomMapper.selectByCondition(query);
        PageInfo<MeetingRoom> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getTotal(), list);
    }

    public MeetingRoom get(Long id) {
        MeetingRoom room = roomMapper.findById(id);
        if (room == null) {
            throw new BusinessException(1001, "会议室不存在");
        }
        return room;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long save(RoomSaveDTO dto) {
        MeetingRoom room = toEntity(dto);
        roomMapper.insert(room);
        return room.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoomSaveDTO dto) {
        get(id);
        MeetingRoom room = toEntity(dto);
        room.setId(id);
        roomMapper.update(room);
    }

    /**
     * 删除前先确认没有历史预定记录：有则引导改为「停用」，避免破坏历史数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        get(id);
        int used = bookMapper.countByRoom(id);
        if (used > 0) {
            throw new BusinessException(1001,
                    "该会议室已有 " + used + " 条预定记录，不能删除，请改为「停用」");
        }
        roomMapper.deleteById(id);
    }

    private MeetingRoom toEntity(RoomSaveDTO dto) {
        MeetingRoom room = new MeetingRoom();
        room.setRoomName(dto.getRoomName().trim());
        room.setCapacity(dto.getCapacity());
        room.setLocation(dto.getLocation().trim());
        room.setEquipment(dto.getEquipment() == null ? "" : dto.getEquipment().trim());
        room.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        room.setRemark(dto.getRemark() == null ? "" : dto.getRemark().trim());
        return room;
    }
}
