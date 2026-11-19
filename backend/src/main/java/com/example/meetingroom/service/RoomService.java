package com.example.meetingroom.service;

import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.RoomStatus;
import com.example.meetingroom.common.SysConfigKey;
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
 * 会议室资源管理：增删改查与状态维护。
 * <p>
 * 两个查询入口共用同一套条件查询，区别只在状态：用户端强制 status=1，
 * 只看得到启用中的会议室；管理端不加状态限制，停用的也要能维护。
 * <p>
 * 删除做了一层保护——只要存在历史预定记录就不允许物理删除，引导改为「停用」，
 * 否则预定表里的 room_id 会指向一条不存在的会议室，历史记录无法追溯。
 * <p>
 * 容纳人数上限来自 sys_config 的 {@code room.max-capacity}，管理员可在线调整，
 * 不再写死在代码或前端控件里。
 */
@Service
public class RoomService {

    @Resource
    private MeetingRoomMapper roomMapper;

    @Resource
    private MeetingBookMapper bookMapper;

    @Resource
    private SysConfigService configService;

    /** 用户端：只能看到启用中的会议室 */
    public PageResult<MeetingRoom> pageForUser(RoomQueryDTO query) {
        query.setStatus(RoomStatus.ENABLED);
        return doPage(query);
    }

    /** 管理端：可查看全部状态 */
    public PageResult<MeetingRoom> pageForAdmin(RoomQueryDTO query) {
        return doPage(query);
    }

    /**
     * 分页查询的公共实现：设置分页参数后执行条件查询，再包装成统一的分页结果。
     * <p>
     * PageHelper 采用 ThreadLocal 传递分页参数，startPage 之后紧跟的第一条查询才会被分页，
     * 所以这里只放一条 select，中间不能插入其它查询，否则分页会作用到错误的语句上。
     */
    private PageResult<MeetingRoom> doPage(RoomQueryDTO query) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<MeetingRoom> list = roomMapper.selectByCondition(query);
        PageInfo<MeetingRoom> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getTotal(), list);
    }

    /**
     * 按主键取会议室，同时充当「存在性断言」：查不到直接抛业务异常。
     * 编辑、删除都先调它，避免对不存在的 id 继续执行写操作。
     */
    public MeetingRoom get(Long id) {
        MeetingRoom room = roomMapper.findById(id);
        if (room == null) {
            throw new BusinessException(1001, "会议室不存在");
        }
        return room;
    }

    /**
     * 新增会议室。状态未填时默认 1（启用），避免新增出来的会议室默认不可见。
     *
     * @return 新插入记录的主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long save(RoomSaveDTO dto) {
        checkCapacity(dto.getCapacity());
        MeetingRoom room = toEntity(dto);
        roomMapper.insert(room);
        return room.getId();
    }

    /**
     * 编辑会议室信息。先确认记录存在，再把表单覆盖到该记录上。
     * <p>
     * 已通过的预定不受影响——改容量或位置不会回头校验历史预定的参会人数，
     * 因为历史记录反映的是当时的实际情况，不应随会议室信息变动而被追溯修改。
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoomSaveDTO dto) {
        get(id);
        checkCapacity(dto.getCapacity());
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

    /**
     * 容纳人数上限校验。
     * <p>
     * 下限 1 由 {@link RoomSaveDTO} 的 {@code @Min(1)} 保证，上限读配置
     * （{@code room.max-capacity}），因为它是管理员可调整的运营参数而非固定规则。
     * 前端控件上的最大值也从同一个参数取，两处口径一致。
     */
    private void checkCapacity(Integer capacity) {
        if (capacity == null) {
            throw new BusinessException(400, "请输入容纳人数");
        }
        int max = configService.getInt(SysConfigKey.ROOM_MAX_CAPACITY);
        if (capacity > max) {
            throw new BusinessException(1001, "容纳人数不能超过 " + max + " 人");
        }
    }

    /**
     * 把保存表单转换成实体，并统一做去空格与默认值处理。
     * <p>
     * 设备与备注允许为空，统一存空串而不是 null，避免前端回显时出现 "null" 字样；
     * 状态为空时按启用处理。
     */
    private MeetingRoom toEntity(RoomSaveDTO dto) {
        MeetingRoom room = new MeetingRoom();
        room.setRoomName(dto.getRoomName().trim());
        room.setCapacity(dto.getCapacity());
        room.setLocation(dto.getLocation().trim());
        room.setEquipment(dto.getEquipment() == null ? "" : dto.getEquipment().trim());
        room.setStatus(dto.getStatus() == null ? RoomStatus.ENABLED : dto.getStatus());
        room.setRemark(dto.getRemark() == null ? "" : dto.getRemark().trim());
        return room;
    }
}
