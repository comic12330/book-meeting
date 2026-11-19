package com.example.meetingroom.service;

import com.example.meetingroom.common.BookStatus;
import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.RoomStatus;
import com.example.meetingroom.common.SysConfigKey;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预定业务：提交、取消、查询与审核。
 * <p>
 * 本类的核心是「同一会议室同一时间段内只能存在一条有效预定记录」这一业务约束。
 * 按任务书要求，该约束不依赖前端时间控件，而是在后端两个环节各校验一次：
 * <ul>
 *   <li><b>提交环节</b> {@link #submit}：把「待审核」也算作占用，重叠的申请当场拒绝，
 *       保证进入待审队列的申请互不冲突；</li>
 *   <li><b>审核环节</b> {@link #audit}：只把「已通过」算作占用，并对会议室行加锁串行化，
 *       保证两条时段重叠的待审申请中只有一条能通过。</li>
 * </ul>
 * 两个环节用的是同一个判定方法 {@link #checkConflict}，差异只在参与判定的状态集合，
 * 见 {@link BookStatus#occupiedOnSubmit()} 与 {@link BookStatus#occupiedOnAudit()}。
 * <p>
 * 时间规则（单次时长上限、最多提前天数、每日可预定窗口）全部来自 sys_config 表，
 * 由管理员在「系统参数」页面维护，见 {@link SysConfigService}。改值即时生效，不需要重启。
 */
@Service
public class BookService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Resource
    private MeetingBookMapper bookMapper;

    @Resource
    private MeetingRoomMapper roomMapper;

    @Resource
    private SysConfigService configService;

    /**
     * 提交预定申请，写入一条状态为 0（待审核）的记录。
     * <p>
     * 校验顺序按「代价从低到高」排列：会议室是否存在且启用 → 时间区间是否合法 →
     * 参会人数是否超过容量 → 时段是否冲突。前三个只需读一条会议室记录或纯内存判断，
     * 冲突校验要扫预定表，放在最后可以避免为明显非法的请求多查一次库。
     * <p>
     * 冲突校验在提交环节执行一次，参与判定的状态是「待审核 + 已通过」，
     * 详见 {@link BookStatus#occupiedOnSubmit()}。
     *
     * @param dto    提交表单，已由 BookSubmitDTO 上的注解做过格式校验
     * @param userId 当前登录用户，由拦截器写入上下文，不接受前端传入
     * @return 新插入记录的主键
     * @throws BusinessException 会议室不存在或已停用、时间非法、超容量、时段冲突时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public Long submit(BookSubmitDTO dto, Long userId) {
        MeetingRoom room = roomMapper.findById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException(1001, "会议室不存在");
        }
        if (!RoomStatus.isEnabled(room.getStatus())) {
            throw new BusinessException(1001, "该会议室已停用，暂不可预定");
        }
        checkTimeRange(dto.getStartTime(), dto.getEndTime());
        if (dto.getParticipantNum() > room.getCapacity()) {
            throw new BusinessException(1001,
                    "参会人数超过会议室容纳上限（" + room.getCapacity() + " 人）");
        }
        checkConflict(dto.getRoomId(), dto.getStartTime(), dto.getEndTime(), null,
                BookStatus.occupiedOnSubmit());

        MeetingBook book = new MeetingBook();
        book.setUserId(userId);
        book.setRoomId(dto.getRoomId());
        book.setStartTime(dto.getStartTime());
        book.setEndTime(dto.getEndTime());
        book.setPurpose(dto.getPurpose().trim());
        book.setParticipantNum(dto.getParticipantNum());
        book.setStatus(BookStatus.PENDING);
        bookMapper.insert(book);
        return book.getId();
    }

    /**
     * 审核预定，把状态从 0（待审核）改为 1（通过）或 2（驳回）。
     * <p>
     * 通过时执行第二次冲突校验，这是全套约束的最后一道闸门：
     * 提交环节虽然已经拦过重叠申请，但两个用户可以并发提交，各自读到「无冲突」后都插入成功，
     * 于是待审队列里可能出现两条时段重叠的申请。此时靠审核环节保证只有一条能通过——
     * 先到的那条把会议室占住，后到的这条在这里被拒绝。
     * <p>
     * 驳回不涉及占用，因此不校验冲突，但必须填写理由，便于申请人知道该改什么。
     *
     * @param bookId 待审核的预定记录主键
     * @param dto    审核结果，status 只能是 1 或 2，驳回时 auditMsg 必填
     * @throws BusinessException 审核结果非法、记录不存在、记录已被处理、驳回未填理由、
     *                           或时段已被其他通过的预定占用时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long bookId, AuditDTO dto) {
        Integer target = dto.getStatus();
        if (!BookStatus.isAuditResult(target)) {
            throw new BusinessException(400, "审核结果只能是通过或驳回");
        }
        MeetingBook book = bookMapper.findById(bookId);
        if (book == null) {
            throw new BusinessException(1001, "预定记录不存在");
        }
        if (!Integer.valueOf(BookStatus.PENDING).equals(book.getStatus())) {
            throw new BusinessException(1001, "该申请已处理，不能重复审核");
        }
        boolean reject = Integer.valueOf(BookStatus.REJECTED).equals(target);
        if (reject && (dto.getAuditMsg() == null || dto.getAuditMsg().trim().isEmpty())) {
            throw new BusinessException(400, "驳回时必须填写理由");
        }
        if (!reject) {
            // 先锁住会议室行，把同一会议室的审核串行化；
            // 两个管理员同时点「通过」时，后一个会等前一个提交完再校验，避免双双通过造成重复占用。
            roomMapper.lockById(book.getRoomId());
            checkConflict(book.getRoomId(), book.getStartTime(), book.getEndTime(),
                    book.getId(), BookStatus.occupiedOnAudit());
        }
        bookMapper.updateStatus(bookId, target,
                dto.getAuditMsg() == null ? null : dto.getAuditMsg().trim(),
                LocalDateTime.now());
    }

    /**
     * 取消预定，把记录状态置为 3（已取消）。
     * <p>
     * 状态为 3 的记录不再参与冲突判定，所以取消等同于释放该时段。
     * 只允许取消「待审核」与「已通过」的记录，并且必须在会议开始之前操作，
     * 已经开始的会议不能事后取消，否则历史记录会失去意义。
     *
     * @param bookId 预定记录主键
     * @param userId 当前登录用户 ID，用于判断是否为本人预定
     * @throws BusinessException 记录不存在、非本人且非管理员、状态不允许、会议已开始时抛出
     */
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
        if (!BookStatus.cancellable(book.getStatus())) {
            throw new BusinessException(1001, "当前状态的预定不可取消");
        }
        if (!book.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(1001, "会议已开始或已结束，不能取消");
        }
        bookMapper.updateStatus(bookId, BookStatus.CANCELLED, null, null);
    }

    /**
     * 用户端：分页查询本人的预定记录，可按状态筛选，按提交时间倒序。
     * <p>
     * 查询条件固定为当前登录用户 ID，即使前端伪造 userId 也查不到他人记录。
     */
    public PageResult<BookVO> pageMyBooks(BookQueryDTO query, Long userId) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<BookVO> list = bookMapper.selectMyBooks(userId, query.getStatus());
        return PageResult.of(new PageInfo<>(list).getTotal(), list);
    }

    /**
     * 管理端：分页查询全部预定，支持按状态、会议室与关键字（申请人或会议用途）筛选。
     * <p>
     * 排序为「状态升序 + 提交时间倒序」（见 MeetingBookMapper.xml），
     * 待审核的记录排在最前，管理员打开页面即可优先处理待办。
     */
    public PageResult<BookVO> pageForAdmin(BookQueryDTO query) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<BookVO> list = bookMapper.selectForAdmin(query.getStatus(), query.getRoomId(), query.getKeyword());
        return PageResult.of(new PageInfo<>(list).getTotal(), list);
    }

    /**
     * 查询单条预定的详情，附带会议室名称、位置与申请人姓名。
     * <p>
     * 越权保护：只有本人和管理员可以查看，防止通过遍历 id 拿到他人的会议安排。
     */
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
     * 时间区间合法性校验。全部在后端判断，不依赖前端时间控件。
     * <p>
     * 四条规则，前两条是固定业务常识，后两条的阈值来自 sys_config，管理员可在线调整：
     * <ol>
     *   <li>结束必须晚于开始；</li>
     *   <li>开始不能早于当前时刻（精确到分钟）；</li>
     *   <li>最多提前 {@code booking.max-advance-days} 天，防止长期占位；</li>
     *   <li>开始时刻必须落在 {@code booking.daily-start} ~ {@code booking.daily-end} 之间。</li>
     * </ol>
     * 另外限制单次跨度不超过 {@code booking.max-hours} 小时。
     * <p>
     * 注意第 4 条只约束<b>开始时刻</b>：任务书要求支持跨天（如 19:00 至次日 01:00），
     * 若把结束时刻也限制在同一天的可预定窗口内，跨天场景就无法提交了。
     * 结束时间通过「跨度上限」间接约束，不会被无限拉长。
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

        int maxAdvanceDays = configService.getInt(SysConfigKey.BOOKING_MAX_ADVANCE_DAYS);
        if (start.isAfter(nowMinute.plusDays(maxAdvanceDays))) {
            throw new BusinessException(1001,
                    "最多只能提前 " + maxAdvanceDays + " 天预定，请调整开始日期");
        }

        LocalTime dailyStart = configService.getTime(SysConfigKey.BOOKING_DAILY_START);
        LocalTime dailyEnd = configService.getTime(SysConfigKey.BOOKING_DAILY_END);
        LocalTime startClock = start.toLocalTime();
        if (startClock.isBefore(dailyStart) || startClock.isAfter(dailyEnd)) {
            throw new BusinessException(1001, "开始时间需在每日 "
                    + dailyStart.format(CLOCK_FORMAT) + " 至 " + dailyEnd.format(CLOCK_FORMAT) + " 之间");
        }

        long maxHours = configService.getLong(SysConfigKey.BOOKING_MAX_HOURS);
        if (Duration.between(start, end).toMinutes() > maxHours * 60) {
            throw new BusinessException(1001, "单次预定不能超过 " + maxHours + " 小时");
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
