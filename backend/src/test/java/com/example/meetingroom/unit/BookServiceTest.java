package com.example.meetingroom.unit;

import com.example.meetingroom.common.BookStatus;
import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.SysConfigKey;
import com.example.meetingroom.dto.AuditDTO;
import com.example.meetingroom.dto.BookSubmitDTO;
import com.example.meetingroom.entity.MeetingBook;
import com.example.meetingroom.entity.MeetingRoom;
import com.example.meetingroom.mapper.MeetingBookMapper;
import com.example.meetingroom.mapper.MeetingRoomMapper;
import com.example.meetingroom.service.BookService;
import com.example.meetingroom.service.SysConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 预定业务规则的单元测试。
 * <p>
 * 用 Mockito 把两个 Mapper 与参数服务替换掉，只验证 {@link BookService} 自己的判断逻辑：
 * 该拒绝的请求有没有拒绝、拒绝的理由对不对、该放行的有没有被误杀。
 * 不启动容器也不连数据库，所以 {@code mvn test} 在任何机器上都能跑。
 * <p>
 * 与接口层测试的分工：这里证明「规则写对了」，接口层证明「规则确实挂在请求路径上」。
 * 冲突查询本身是一条 SQL，属持久层行为，由接口测试连真实数据库验证，此处只模拟返回条数。
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final Long ROOM_ID = 1L;

    private static final Long BOOK_ID = 100L;

    private static final Long USER_ID = 2L;

    @Mock
    private MeetingBookMapper bookMapper;

    @Mock
    private MeetingRoomMapper roomMapper;

    @Mock
    private SysConfigService configService;

    @InjectMocks
    private BookService bookService;

    // ==================== 提交环节 ====================

    @Test
    @DisplayName("提交：会议室不存在时拒绝")
    void submitWithUnknownRoom() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.submit(dto(tomorrow(9), tomorrow(10), 5), USER_ID));

        assertEquals(1001, e.getCode());
        assertTrue(e.getMessage().contains("会议室不存在"));
        verify(bookMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交：停用的会议室不接受预定")
    void submitToDisabledRoom() {
        MeetingRoom room = room(20, 0);
        when(roomMapper.findById(ROOM_ID)).thenReturn(room);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.submit(dto(tomorrow(9), tomorrow(10), 5), USER_ID));

        assertTrue(e.getMessage().contains("已停用"));
        verify(bookMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交：结束时间不晚于开始时间时拒绝")
    void submitWithReversedRange() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(room(20, 1));

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.submit(dto(tomorrow(10), tomorrow(10), 5), USER_ID));

        assertTrue(e.getMessage().contains("结束时间必须晚于开始时间"));
        verify(bookMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交：开始时间早于当前时刻时拒绝")
    void submitInThePast() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(room(20, 1));
        LocalDateTime past = LocalDateTime.now().minusHours(2);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.submit(dto(past, past.plusHours(1), 5), USER_ID));

        assertTrue(e.getMessage().contains("不能早于当前时间"));
        verify(bookMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交：超出最多提前天数时拒绝")
    void submitTooFarInAdvance() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(room(20, 1));
        when(configService.getInt(SysConfigKey.BOOKING_MAX_ADVANCE_DAYS)).thenReturn(3);
        LocalDateTime start = LocalDate.now().plusDays(10).atTime(9, 0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.submit(dto(start, start.plusHours(1), 5), USER_ID));

        assertTrue(e.getMessage().contains("3 天"), "提示语要带上实际配置的天数，实际：" + e.getMessage());
        verify(bookMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交：开始时刻不在每日可预定窗口内时拒绝")
    void submitOutsideDailyWindow() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(room(20, 1));
        // 窗口检查在时长检查之前抛出，getLong 不会被调用，这里只给前三项
        when(configService.getInt(SysConfigKey.BOOKING_MAX_ADVANCE_DAYS)).thenReturn(30);
        when(configService.getTime(SysConfigKey.BOOKING_DAILY_START)).thenReturn(LocalTime.parse("08:00"));
        when(configService.getTime(SysConfigKey.BOOKING_DAILY_END)).thenReturn(LocalTime.parse("22:00"));
        LocalDateTime start = tomorrow(7);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.submit(dto(start, start.plusHours(1), 5), USER_ID));

        assertTrue(e.getMessage().contains("08:00"));
        verify(bookMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交：单次跨度超上限时拒绝，提示语带配置的小时数")
    void submitExceedsMaxHours() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(room(20, 1));
        stubTimeRangeConfig(30, "08:00", "22:00", 2);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.submit(dto(tomorrow(9), tomorrow(13), 5), USER_ID));

        assertTrue(e.getMessage().contains("不能超过 2 小时"));
        verify(bookMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交：跨天时段在跨度上限内时放行")
    void submitOvernightIsAllowed() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(room(20, 1));
        stubTimeRangeConfig(30, "08:00", "22:00", 8);
        when(bookMapper.countConflict(eq(ROOM_ID), any(), any(), isNull(), anyList())).thenReturn(0);
        stubInsertReturningId();

        // 19:00 至次日 01:00，跨天但只有 6 小时
        LocalDateTime start = tomorrow(19);
        Long id = bookService.submit(dto(start, start.plusHours(6), 5), USER_ID);

        assertEquals(99L, id);
        verify(bookMapper).countConflict(eq(ROOM_ID), eq(start), eq(start.plusHours(6)),
                isNull(), eq(BookStatus.occupiedOnSubmit()));
    }

    @Test
    @DisplayName("提交：参会人数超过容纳上限时拒绝")
    void submitExceedsCapacity() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(room(10, 1));
        stubTimeRangeConfig(30, "08:00", "22:00", 8);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.submit(dto(tomorrow(9), tomorrow(10), 11), USER_ID));

        assertTrue(e.getMessage().contains("10 人"));
        verify(bookMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交：时段冲突时拒绝，且不产生新记录")
    void submitWithConflict() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(room(20, 1));
        stubTimeRangeConfig(30, "08:00", "22:00", 8);
        when(bookMapper.countConflict(eq(ROOM_ID), any(), any(), isNull(), anyList())).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.submit(dto(tomorrow(9), tomorrow(10), 5), USER_ID));

        assertTrue(e.getMessage().contains("已被预定"));
        verify(bookMapper, never()).insert(any());
    }

    @Test
    @DisplayName("提交：校验通过时写入待审核记录并回填主键")
    void submitSuccess() {
        when(roomMapper.findById(ROOM_ID)).thenReturn(room(20, 1));
        stubTimeRangeConfig(30, "08:00", "22:00", 8);
        when(bookMapper.countConflict(eq(ROOM_ID), any(), any(), isNull(), anyList())).thenReturn(0);
        stubInsertReturningId();

        Long id = bookService.submit(dto(tomorrow(9), tomorrow(10), 5), USER_ID);

        ArgumentCaptor<MeetingBook> captor = ArgumentCaptor.forClass(MeetingBook.class);
        verify(bookMapper).insert(captor.capture());
        MeetingBook saved = captor.getValue();
        assertEquals(99L, id);
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(ROOM_ID, saved.getRoomId());
        assertEquals(BookStatus.PENDING, saved.getStatus(), "新提交的记录必须是待审核");
        assertEquals("项目组周会", saved.getPurpose(), "用途入库前要经过 trim");
    }

    // ==================== 审核环节 ====================

    @Test
    @DisplayName("审核通过：记录已不在待审核状态时拒绝重复审核")
    void approveAlreadyHandled() {
        MeetingBook book = pendingBook(tomorrow(9), tomorrow(10));
        book.setStatus(BookStatus.APPROVED);
        when(bookMapper.findById(BOOK_ID)).thenReturn(book);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.approve(BOOK_ID, auditDto(BookStatus.APPROVED, null)));

        assertTrue(e.getMessage().contains("不能重复审核"));
        verify(bookMapper, never()).updateStatus(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("审核通过：使用时间已结束的申请不能再通过")
    void approveExpiredBooking() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        MeetingBook book = pendingBook(start, start.plusHours(1));
        when(bookMapper.findById(BOOK_ID)).thenReturn(book);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.approve(BOOK_ID, auditDto(BookStatus.APPROVED, null)));

        assertTrue(e.getMessage().contains("使用时间已结束"));
        verify(bookMapper, never()).updateStatus(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("审核通过：时段已被占用时拒绝，且先锁住会议室行")
    void approveConflict() {
        MeetingBook book = pendingBook(tomorrow(9), tomorrow(10));
        when(bookMapper.findById(BOOK_ID)).thenReturn(book);
        when(roomMapper.lockById(ROOM_ID)).thenReturn(ROOM_ID);
        when(bookMapper.countConflict(eq(ROOM_ID), any(), any(), eq(BOOK_ID), anyList())).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.approve(BOOK_ID, auditDto(BookStatus.APPROVED, null)));

        assertTrue(e.getMessage().contains("已被预定"));
        verify(roomMapper).lockById(ROOM_ID);
        verify(bookMapper, never()).updateStatus(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("审核通过：无冲突时写入通过状态，冲突判定只统计已通过")
    void approveSuccess() {
        MeetingBook book = pendingBook(tomorrow(9), tomorrow(10));
        when(bookMapper.findById(BOOK_ID)).thenReturn(book);
        when(roomMapper.lockById(ROOM_ID)).thenReturn(ROOM_ID);
        when(bookMapper.countConflict(eq(ROOM_ID), any(), any(), eq(BOOK_ID),
                eq(BookStatus.occupiedOnAudit()))).thenReturn(0);

        bookService.approve(BOOK_ID, auditDto(BookStatus.APPROVED, " 同意 "));

        verify(bookMapper).updateStatus(eq(BOOK_ID), eq(BookStatus.APPROVED), eq("同意"), any());
    }

    @Test
    @DisplayName("审核通过：结果传驳回以外的非法值时拒绝")
    void approveWithIllegalStatus() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.approve(BOOK_ID, auditDto(BookStatus.CANCELLED, null)));

        assertEquals(400, e.getCode());
        assertTrue(e.getMessage().contains("只能是通过或驳回"));
    }

    @Test
    @DisplayName("驳回：必须填写理由")
    void rejectWithoutReason() {
        when(bookMapper.findById(BOOK_ID)).thenReturn(pendingBook(tomorrow(9), tomorrow(10)));

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.reject(BOOK_ID, auditDto(BookStatus.REJECTED, "   ")));

        assertEquals(400, e.getCode());
        assertTrue(e.getMessage().contains("必须填写理由"));
        verify(bookMapper, never()).updateStatus(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("驳回：已结束的待审申请允许驳回，作为清理队列的出口")
    void rejectExpiredBookingIsAllowed() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        when(bookMapper.findById(BOOK_ID)).thenReturn(pendingBook(start, start.plusHours(1)));

        bookService.reject(BOOK_ID, auditDto(BookStatus.REJECTED, "超过使用时间仍未审核"));

        verify(bookMapper).updateStatus(eq(BOOK_ID), eq(BookStatus.REJECTED),
                eq("超过使用时间仍未审核"), any());
    }

    // ==================== 取消环节 ====================

    @Test
    @DisplayName("取消：不能取消他人的预定")
    void cancelOthersBooking() {
        when(bookMapper.findById(BOOK_ID)).thenReturn(pendingBook(tomorrow(9), tomorrow(10)));

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.cancel(BOOK_ID, USER_ID + 1));

        assertEquals(403, e.getCode());
        assertTrue(e.getMessage().contains("只能取消本人"));
        verify(bookMapper, never()).cancelById(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("取消：会议已开始时拒绝")
    void cancelStartedBooking() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        MeetingBook book = pendingBook(start, start.plusHours(2));
        when(bookMapper.findById(BOOK_ID)).thenReturn(book);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.cancel(BOOK_ID, USER_ID));

        assertTrue(e.getMessage().contains("不能取消"));
        verify(bookMapper, never()).cancelById(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("取消：只写状态与取消时间，不动审核痕迹")
    void cancelKeepsAuditTrace() {
        MeetingBook book = pendingBook(tomorrow(9), tomorrow(10));
        book.setStatus(BookStatus.APPROVED);
        book.setAuditMsg("同意");
        book.setAuditTime(LocalDateTime.now().minusHours(1));
        when(bookMapper.findById(BOOK_ID)).thenReturn(book);

        bookService.cancel(BOOK_ID, USER_ID);

        // 本人取消没有理由，取消时间由服务层取当前时刻
        verify(bookMapper).cancelById(eq(BOOK_ID), eq(BookStatus.CANCELLED), isNull(), any());
        // 关键：绝不能复用 updateStatus，那会把 audit_msg / audit_time 清空
        verify(bookMapper, never()).updateStatus(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("取消：已取消的记录不能再次取消")
    void cancelTwice() {
        MeetingBook book = pendingBook(tomorrow(9), tomorrow(10));
        book.setStatus(BookStatus.CANCELLED);
        when(bookMapper.findById(BOOK_ID)).thenReturn(book);

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.cancel(BOOK_ID, USER_ID));

        assertTrue(e.getMessage().contains("不可取消"));
    }

    @Test
    @DisplayName("管理员取消：理由为空时拒绝")
    void adminCancelWithoutReason() {
        com.example.meetingroom.dto.CancelDTO dto = new com.example.meetingroom.dto.CancelDTO();
        dto.setReason("  ");

        BusinessException e = assertThrows(BusinessException.class,
                () -> bookService.adminCancel(BOOK_ID, dto));

        assertEquals(400, e.getCode());
        assertTrue(e.getMessage().contains("必须填写理由"));
        verify(bookMapper, never()).cancelById(anyLong(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("管理员取消：理由落库，供申请人在「我的预定」看到")
    void adminCancelWithReason() {
        when(bookMapper.findById(BOOK_ID)).thenReturn(pendingBook(tomorrow(9), tomorrow(10)));
        com.example.meetingroom.dto.CancelDTO dto = new com.example.meetingroom.dto.CancelDTO();
        dto.setReason(" 会议室临时征用 ");

        bookService.adminCancel(BOOK_ID, dto);

        verify(bookMapper).cancelById(eq(BOOK_ID), eq(BookStatus.CANCELLED),
                eq("会议室临时征用"), any());
    }

    // ==================== 定时任务 ====================

    @Test
    @DisplayName("扫描过期待审：只处理已通过的记录")
    void completeExpiredDelegatesToMapper() {
        when(bookMapper.completeExpired(BookStatus.APPROVED, BookStatus.COMPLETED)).thenReturn(3);

        int updated = bookService.completeExpired();

        assertEquals(3, updated);
        verify(bookMapper).completeExpired(BookStatus.APPROVED, BookStatus.COMPLETED);
    }

    // ------------------------------------------------------------ 构造测试数据

    /** 明天的某个整点，避开「开始时间早于当前时间」的限制 */
    private LocalDateTime tomorrow(int hour) {
        return LocalDate.now().plusDays(1).atTime(hour, 0);
    }

    private MeetingRoom room(int capacity, int status) {
        MeetingRoom room = new MeetingRoom();
        room.setId(ROOM_ID);
        room.setRoomName("第一会议室");
        room.setCapacity(capacity);
        room.setLocation("行政楼 3 层 301");
        room.setStatus(status);
        return room;
    }

    private BookSubmitDTO dto(LocalDateTime start, LocalDateTime end, int participants) {
        BookSubmitDTO dto = new BookSubmitDTO();
        dto.setRoomId(ROOM_ID);
        dto.setStartTime(start);
        dto.setEndTime(end);
        dto.setPurpose("  项目组周会  ");
        dto.setParticipantNum(participants);
        return dto;
    }

    private AuditDTO auditDto(int status, String msg) {
        AuditDTO dto = new AuditDTO();
        dto.setStatus(status);
        dto.setAuditMsg(msg);
        return dto;
    }

    private MeetingBook pendingBook(LocalDateTime start, LocalDateTime end) {
        MeetingBook book = new MeetingBook();
        book.setId(BOOK_ID);
        book.setUserId(USER_ID);
        book.setRoomId(ROOM_ID);
        book.setStartTime(start);
        book.setEndTime(end);
        book.setPurpose("项目组周会");
        book.setParticipantNum(5);
        book.setStatus(BookStatus.PENDING);
        return book;
    }

    /** 时间区间的四个阈值都来自 sys_config，提交成功路径必须全部给到 */
    private void stubTimeRangeConfig(int maxDays, String dailyStart, String dailyEnd, long maxHours) {
        when(configService.getInt(SysConfigKey.BOOKING_MAX_ADVANCE_DAYS)).thenReturn(maxDays);
        when(configService.getTime(SysConfigKey.BOOKING_DAILY_START)).thenReturn(LocalTime.parse(dailyStart));
        when(configService.getTime(SysConfigKey.BOOKING_DAILY_END)).thenReturn(LocalTime.parse(dailyEnd));
        when(configService.getLong(SysConfigKey.BOOKING_MAX_HOURS)).thenReturn(maxHours);
    }

    /** 模拟 MyBatis 回填自增主键 */
    private void stubInsertReturningId() {
        when(bookMapper.insert(any(MeetingBook.class))).thenAnswer(invocation -> {
            MeetingBook book = invocation.getArgument(0);
            book.setId(99L);
            return 1;
        });
    }
}
