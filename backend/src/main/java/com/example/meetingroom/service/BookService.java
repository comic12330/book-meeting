package com.example.meetingroom.service;

import com.example.meetingroom.annotation.LogOperation;
import com.example.meetingroom.common.BookStatus;
import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.OperationType;
import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.RoomStatus;
import com.example.meetingroom.common.SysConfigKey;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.dto.AuditDTO;
import com.example.meetingroom.dto.BookQueryDTO;
import com.example.meetingroom.dto.BookSubmitDTO;
import com.example.meetingroom.dto.CancelDTO;
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
 *
 * 本类的核心是「同一会议室同一时间段内只能存在一条有效预定记录」这一业务约束。
 * 按任务书要求，该约束不依赖前端时间控件，而是在后端两个环节各校验一次：提交环节 {@link #submit} 把「待审核」也算作占用，重叠的申请当场拒绝，保证进入待审队列的申请互不冲突；审核环节 {@link #approve} 只把「已通过」算作占用，并对会议室行加锁串行化，保证两条时段重叠的待审申请中只有一条能通过。
 * 两个环节用的是同一个判定方法 {@link #checkConflict}，差异只在参与判定的状态集合，见 {@link BookStatus#occupiedOnSubmit()} 与 {@link BookStatus#occupiedOnAudit()}。
 * 时间规则（单次时长上限、最多提前天数、每日可预定窗口）全部来自 sys_config 表，由管理员在「系统参数」页面维护，见 {@link SysConfigService}。改值即时生效，不需要重启。
 */
@Service
public class BookService {

    /** 日志描述里的日期时间格式 */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 每日可预定窗口的时分格式 */
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Resource
    private MeetingBookMapper bookMapper;

    @Resource
    private MeetingRoomMapper roomMapper;

    @Resource
    private SysConfigService configService;

    /**
     * 提交预定申请，写入一条状态为 0（待审核）的记录。
     * 校验顺序按代价从低到高排列：会议室是否存在且启用 → 时间区间是否合法 →参会人数是否超过容量 → 时段是否冲突。前三个只需读一条会议室记录或纯内存判断，冲突校验要扫预定表，放在最后可以避免为明显非法的请求多查一次库。
     * 冲突校验在提交环节执行一次，参与判定的状态是「待审核 + 已通过」，详见 {@link BookStatus#occupiedOnSubmit()}。
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
     * 审核通过：把状态从 0（待审核）改为 1（通过），并执行第二次冲突校验。
     * 这是全套约束的最后一道闸门。提交环节虽然已经拦过重叠申请，但两个用户可以并发提交，各自读到「无冲突」后都插入成功，于是待审队列里可能出现两条时段重叠的申请。
     * 此时靠这一环节保证只有一条能通过：先到的那条把会议室占住，后到的在这里被拒绝。
     * 「通过」与「驳回」拆成两个方法，是因为操作日志要按动作区分类型：一个方法只能挂一个注解，而管理员最常筛的就是谁批的、谁驳的。
     *
     * @param bookId 待审核的预定记录主键
     * @param dto    审核结果，status 必须为 1
     * @throws BusinessException 结果非法、记录不存在、记录已被处理、使用时间已结束、或时段已被其他通过的预定占用时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    @LogOperation(type = OperationType.BOOK_AUDIT_APPROVE, target = "describe(#bookId)")
    public void approve(Long bookId, AuditDTO dto) {
        if (!Integer.valueOf(BookStatus.APPROVED).equals(dto.getStatus())) {
            throw new BusinessException(400, "审核结果只能是通过或驳回");
        }
        MeetingBook book = requirePending(bookId);
        // 已结束的申请不能再通过：否则通过后下一次定时任务立刻把它置为已完成，状态相当于从「待审核」直接跳到「已完成」，历史记录里看不出中间通过过。
        // 这里只拦「通过」不拦「驳回」——驳回是清理过期待审队列的唯一出口，若把驳回也拦住，这些记录就只能进数据库手改。
        if (!book.getEndTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(1001, "该申请的使用时间已结束，不能再审核为通过");
        }
        // 先锁住会议室行，把同一会议室的审核串行化；两个管理员同时点「通过」时，后一个会等前一个提交完再校验，避免双双通过造成重复占用。
        roomMapper.lockById(book.getRoomId());
        checkConflict(book.getRoomId(), book.getStartTime(), book.getEndTime(),
                book.getId(), BookStatus.occupiedOnAudit());
        bookMapper.updateStatus(bookId, BookStatus.APPROVED, trimToNull(dto.getAuditMsg()),
                LocalDateTime.now());
    }

    /**
     * 审核驳回：把状态从 0（待审核）改为 2（驳回）。
     * 驳回不涉及占用，因此不校验冲突，但必须填写理由，便于申请人知道该改什么。
     * 已结束的申请允许驳回，原因见 {@link #approve} 里的说明。
     *
     * @param bookId 待审核的预定记录主键
     * @param dto    审核结果，status 必须为 2，auditMsg 必填
     * @throws BusinessException 结果非法、记录不存在、记录已被处理或未填理由时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    @LogOperation(type = OperationType.BOOK_AUDIT_REJECT, target = "describe(#bookId)")
    public void reject(Long bookId, AuditDTO dto) {
        if (!Integer.valueOf(BookStatus.REJECTED).equals(dto.getStatus())) {
            throw new BusinessException(400, "审核结果只能是通过或驳回");
        }
        requirePending(bookId);
        if (dto.getAuditMsg() == null || dto.getAuditMsg().trim().isEmpty()) {
            throw new BusinessException(400, "驳回时必须填写理由");
        }
        bookMapper.updateStatus(bookId, BookStatus.REJECTED, dto.getAuditMsg().trim(),
                LocalDateTime.now());
    }

    /**
     * 取消预定，把记录状态置为 3（已取消）。
     * 状态为 3 的记录不再参与冲突判定，所以取消等同于释放该时段。
     * 只允许取消「待审核」与「已通过」的记录，并且必须在会议开始之前操作，已经开始的会议不能事后取消，否则历史记录会失去意义。
     * 本方法只处理本人取消本人。管理员取消他人的预定走管理端的 {@link #adminCancel}，那个动作影响他人利益，必须留下理由，且要记入操作日志。
     * 取消只写 status / cancel_reason / cancel_time 三列，不再复用审核用的 updateStatus，否则「审核通过 → 取消」的记录会丢掉审核时间。
     *
     * @param bookId 预定记录主键
     * @param userId 当前登录用户 ID
     * @throws BusinessException 记录不存在、非本人、状态不允许、会议已开始时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long bookId, Long userId) {
        MeetingBook book = requireBook(bookId);
        if (!book.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能取消本人的预定");
        }
        ensureCancellable(book);
        bookMapper.cancelById(bookId, BookStatus.CANCELLED, null, LocalDateTime.now());
    }

    /**
     * 管理员取消他人的预定（行政取消）。
     * 这个能力必须保留：员工离职、会议室被临时征用、重要接待需要清场，都属于「本人已经联系不上但会议室还被占着」的情形，收回权限就只能进数据库手改。
     * 但它是有代价的动作，因此做了三条约束：只挂在 {@code /api/admin/**} 下，与其它管理动作一致，普通用户调不到；理由必填并落库，被取消的人能在「我的预定」里看到为什么被取消；记入操作日志，事后可查是谁在什么时候取消了谁的会议。
     *
     * @param bookId 预定记录主键
     * @param dto    取消理由，必填
     * @throws BusinessException 理由为空、记录不存在、状态不允许、会议已开始时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    @LogOperation(type = OperationType.BOOK_CANCEL_ADMIN, target = "describe(#bookId)")
    public void adminCancel(Long bookId, CancelDTO dto) {
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new BusinessException(400, "取消他人预定必须填写理由");
        }
        MeetingBook book = requireBook(bookId);
        ensureCancellable(book);
        bookMapper.cancelById(bookId, BookStatus.CANCELLED, dto.getReason().trim(),
                LocalDateTime.now());
    }

    /**
     * 把使用时间已结束的「已通过」预定批量置为「已完成」（状态 1 → 4）。
     * 由定时任务 {@code com.example.meetingroom.task.BookStatusTask} 周期调用，管理端另有一个手动触发接口，两者共用本方法，避免把同一段 SQL 写两遍。
     * 幂等由 SQL 条件保证：判定是 {@code status = 1 AND end_time < NOW()}，已经是 4 的记录不再命中，重复调用影响 0 行；多实例并发也是后到的影响 0 行，不需要分布式锁。也不需要补跑机制，条件基于数据本身而不是「上次执行到哪」，服务停机期间漏掉的记录，下次启动后第一次执行会自动补齐。
     * 只改 status 一列，audit_msg / audit_time 原样保留，所以没有复用 {@link MeetingBookMapper#updateStatus}。
     *
     * @return 本次被置为已完成的记录条数，0 表示没有待处理的记录
     */
    @Transactional(rollbackFor = Exception.class)
    public int completeExpired() {
        return bookMapper.completeExpired(BookStatus.APPROVED, BookStatus.COMPLETED);
    }

    /**
     * 用户端：分页查询本人的预定记录，可按状态筛选，按提交时间倒序。
     * 查询条件固定为当前登录用户 ID，即使前端伪造 userId 也查不到他人记录。
     */
    public PageResult<BookVO> pageMyBooks(BookQueryDTO query, Long userId) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<BookVO> list = bookMapper.selectMyBooks(userId, query.getStatus());
        return PageResult.of(new PageInfo<>(list).getTotal(), list);
    }

    /**
     * 管理端：分页查询全部预定，支持按状态、会议室与关键字（申请人或会议用途）筛选。
     * 排序为「状态升序 + 提交时间倒序」（见 MeetingBookMapper.xml），待审核的记录排在最前，管理员打开页面即可优先处理待办。
     */
    public PageResult<BookVO> pageForAdmin(BookQueryDTO query) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<BookVO> list = bookMapper.selectForAdmin(query.getStatus(), query.getRoomId(), query.getKeyword());
        return PageResult.of(new PageInfo<>(list).getTotal(), list);
    }

    /**
     * 查询单条预定的详情，附带会议室名称、位置与申请人姓名。
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
     * 供操作日志取对象的可读描述，形如「第一会议室 2026-11-20 09:00 至 10:00」。
     * 由切面的 SpEL 表达式调用（见 {@code @LogOperation(target = "describe(#bookId)")}）。
     * 审核、取消这些方法只拿到主键，光记一个 id 事后看不出动的是哪场会议。
     * 取不到时返回 null，切面会忽略，不影响业务。
     */
    public String describe(Long bookId) {
        BookVO vo = bookMapper.findVOById(bookId);
        if (vo == null) {
            return null;
        }
        return vo.getRoomName() + " " + vo.getStartTime().format(TIME_FORMAT)
                + " 至 " + vo.getEndTime().format(TIME_FORMAT);
    }

    /** 按主键取记录，查不到直接抛业务异常，作为写操作前的存在性断言 */
    private MeetingBook requireBook(Long bookId) {
        MeetingBook book = bookMapper.findById(bookId);
        if (book == null) {
            throw new BusinessException(1001, "预定记录不存在");
        }
        return book;
    }

    /** 审核前断言：记录必须处于「待审核」，已处理过的不能重复审核 */
    private MeetingBook requirePending(Long bookId) {
        MeetingBook book = requireBook(bookId);
        if (!Integer.valueOf(BookStatus.PENDING).equals(book.getStatus())) {
            throw new BusinessException(1001, "该申请已处理，不能重复审核");
        }
        return book;
    }

    /**
     * 取消前的共同断言：状态允许取消，且会议尚未开始。
     * 本人取消与管理员取消共用，保证两条路径的规则不会各走一套。
     */
    private void ensureCancellable(MeetingBook book) {
        if (!BookStatus.cancellable(book.getStatus())) {
            throw new BusinessException(1001, "当前状态的预定不可取消");
        }
        if (!book.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(1001, "会议已开始或已结束，不能取消");
        }
    }

    /** 去空格，空白串按 null 处理，避免库里出现空字符串 */
    private String trimToNull(String text) {
        return text == null || text.trim().isEmpty() ? null : text.trim();
    }

    /**
     * 时间区间合法性校验。全部在后端判断，不依赖前端时间控件。
     * 四条规则里，前两条是固定业务常识，后两条的阈值来自 sys_config，管理员可在线调整：结束必须晚于开始；开始不能早于当前时刻（精确到分钟）；最多提前 {@code booking.max-advance-days} 天，防止长期占位；开始时刻必须落在 {@code booking.daily-start} ~ {@code booking.daily-end} 之间。
     * 另外限制单次跨度不超过 {@code booking.max-hours} 小时。
     * 第四条只约束开始时刻：任务书要求支持跨天（如 19:00 至次日 01:00），若把结束时刻也限制在同一天的可预定窗口内，跨天场景就无法提交。
     * 结束时间通过跨度上限间接约束，不会被无限拉长。
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
