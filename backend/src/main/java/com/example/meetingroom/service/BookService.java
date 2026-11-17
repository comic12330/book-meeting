package com.example.meetingroom.service;

import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.dto.AuditDTO;
import com.example.meetingroom.dto.BookQueryDTO;
import com.example.meetingroom.dto.BookSubmitDTO;
import com.example.meetingroom.entity.MeetingBook;
import com.example.meetingroom.entity.MeetingRoom;
import com.example.meetingroom.mapper.MeetingBookMapper;
import com.example.meetingroom.mapper.MeetingRoomMapper;
import com.example.meetingroom.vo.BookVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 预定业务：提交、取消、审核。核心是会议室时段冲突校验。
 */
@Service
public class BookService {

    /** 单次预定最长时长（小时），防止误填超长时间段 */
    private static final long MAX_HOURS = 72L;

    /** 提交环节参与冲突判定的状态：待审核与已通过都算占用，避免同一时段被反复提交 */
    private static final List<Integer> OCCUPIED_ON_SUBMIT =
            Collections.unmodifiableList(Arrays.asList(0, 1));

    /** 审核环节参与冲突判定的状态：只有已通过才算真正占用 */
    private static final List<Integer> OCCUPIED_ON_AUDIT =
            Collections.singletonList(1);

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Resource
    private MeetingBookMapper bookMapper;

    @Resource
    private MeetingRoomMapper roomMapper;

    /**
     * 提交预定。校验顺序：会议室可用性 → 时间合法性 → 容量 → 时段冲突。
     * 冲突校验在提交环节执行一次。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long submit(BookSubmitDTO dto, Long userId) {
        MeetingRoom room = roomMapper.findById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException(1001, "会议室不存在");
        }
        if (!Integer.valueOf(1).equals(room.getStatus())) {
            throw new BusinessException(1001, "该会议室已停用，暂不可预定");
        }
        checkTimeRange(dto.getStartTime(), dto.getEndTime());
        if (dto.getParticipantNum() > room.getCapacity()) {
            throw new BusinessException(1001,
                    "参会人数超过会议室容纳上限（" + room.getCapacity() + " 人）");
        }
        checkConflict(dto.getRoomId(), dto.getStartTime(), dto.getEndTime(), null, OCCUPIED_ON_SUBMIT);

        MeetingBook book = new MeetingBook();
        book.setUserId(userId);
        book.setRoomId(dto.getRoomId());
        book.setStartTime(dto.getStartTime());
        book.setEndTime(dto.getEndTime());
        book.setPurpose(dto.getPurpose().trim());
        book.setParticipantNum(dto.getParticipantNum());
        book.setStatus(0);
        bookMapper.insert(book);
        return book.getId();
    }

    /**
     * 审核预定。通过时执行第二次冲突校验：
     * 防止两条「待审核」记录时段重叠、先被通过的那条占位后，后一条依然被通过。
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long bookId, AuditDTO dto) {
        Integer target = dto.getStatus();
        if (!Integer.valueOf(1).equals(target) && !Integer.valueOf(2).equals(target)) {
            throw new BusinessException(400, "审核结果只能是通过或驳回");
        }
        MeetingBook book = bookMapper.findById(bookId);
        if (book == null) {
            throw new BusinessException(1001, "预定记录不存在");
        }
        if (!Integer.valueOf(0).equals(book.getStatus())) {
            throw new BusinessException(1001, "该申请已处理，不能重复审核");
        }
        boolean reject = Integer.valueOf(2).equals(target);
        if (reject && (dto.getAuditMsg() == null || dto.getAuditMsg().trim().isEmpty())) {
            throw new BusinessException(400, "驳回时必须填写理由");
        }
        if (!reject) {
            // 先锁住会议室行，把同一会议室的审核串行化；
            // 两个管理员同时点「通过」时，后一个会等前一个提交完再校验，避免双双通过造成重复占用。
            roomMapper.lockById(book.getRoomId());
            checkConflict(book.getRoomId(), book.getStartTime(), book.getEndTime(),
                    book.getId(), OCCUPIED_ON_AUDIT);
        }
        bookMapper.updateStatus(bookId, target,
                dto.getAuditMsg() == null ? null : dto.getAuditMsg().trim(),
                LocalDateTime.now());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long bookId, Long userId) {
        MeetingBook book = bookMapper.findById(bookId);
        if (book == null) {
            throw new BusinessException(1001, "预定记录不存在");
        }
        boolean self = book.getUserId().equals(userId);
        if (!self && !UserContext.isAdmin()) {
            throw new BusinessException(403, "只能取消本人的预定");
        }
        if (!Integer.valueOf(0).equals(book.getStatus()) && !Integer.valueOf(1).equals(book.getStatus())) {
            throw new BusinessException(1001, "当前状态的预定不可取消");
        }
        if (!book.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(1001, "会议已开始或已结束，不能取消");
        }
        bookMapper.updateStatus(bookId, 3, null, null);
    }

    public PageResult<BookVO> pageMyBooks(BookQueryDTO query, Long userId) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<BookVO> list = bookMapper.selectMyBooks(userId, query.getStatus());
        return PageResult.of(new PageInfo<>(list).getTotal(), list);
    }

    public PageResult<BookVO> pageForAdmin(BookQueryDTO query) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<BookVO> list = bookMapper.selectForAdmin(query.getStatus(), query.getRoomId(), query.getKeyword());
        return PageResult.of(new PageInfo<>(list).getTotal(), list);
    }

    public BookVO getVO(Long id) {
        BookVO vo = bookMapper.findVOById(id);
        if (vo == null) {
            throw new BusinessException(1001, "预定记录不存在");
        }
        if (!vo.getUserId().equals(UserContext.userId()) && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权查看他人的预定记录");
        }
        return vo;
    }

    // ------------------------------------------------------------ 私有校验逻辑

    /**
     * 时间区间合法性：结束晚于开始、不早于当前时间、单次不超过上限。
     * 全部在后端判断，不依赖前端时间控件。
     */
    private void checkTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BusinessException(400, "请选择完整的使用时间");
        }
        if (!end.isAfter(start)) {
            throw new BusinessException(1001, "结束时间必须晚于开始时间");
        }
        LocalDateTime nowMinute = LocalDateTime.now().withSecond(0).withNano(0);
        if (start.isBefore(nowMinute)) {
            throw new BusinessException(1001, "开始时间不能早于当前时间");
        }
        long hours = Duration.between(start, end).toHours();
        if (hours > MAX_HOURS) {
            throw new BusinessException(1001, "单次预定不能超过 " + MAX_HOURS + " 小时");
        }
    }

    /**
     * 冲突校验：同一会议室在同一时间段内只能存在一条有效记录。
     * 判定交给数据库完成（countConflict），避免「先查再判」带来的并发窗口。
     *
     * @param statuses  参与判定的状态集合，提交传 [0,1]，审核只传 [1]
     * @param excludeId 审核时排除记录自身，提交传 null
     */
    private void checkConflict(Long roomId, LocalDateTime start, LocalDateTime end,
                               Long excludeId, List<Integer> statuses) {
        int conflict = bookMapper.countConflict(roomId, start, end, excludeId, statuses);
        if (conflict > 0) {
            throw new BusinessException(1001, "该会议室在 "
                    + start.format(TIME_FORMAT) + " 至 " + end.format(TIME_FORMAT)
                    + " 之间已被预定，请调整时间");
        }
    }
}
