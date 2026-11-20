package com.example.meetingroom.unit;

import com.example.meetingroom.common.BookStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 预定状态字典的单元测试。
 * <p>
 * 重点不是常量值本身，而是两个「参与冲突判定的状态集合」：
 * 提交环节必须包含待审核（防止同一时段被反复提交），审核环节必须只有已通过
 * （否则两条重叠的待审记录会互相阻断，永远无法通过）。
 * 这两条是业务规则，改动会让冲突校验整体失效，必须有测试守着。
 */
class BookStatusTest {

    @Test
    @DisplayName("提交环节：待审核与已通过都算占用")
    void occupiedOnSubmitContainsPendingAndApproved() {
        assertEquals(Arrays.asList(BookStatus.PENDING, BookStatus.APPROVED),
                BookStatus.occupiedOnSubmit());
    }

    @Test
    @DisplayName("审核环节：只有已通过算占用，待审核不能参与")
    void occupiedOnAuditContainsOnlyApproved() {
        assertEquals(Arrays.asList(BookStatus.APPROVED), BookStatus.occupiedOnAudit());
        assertFalse(BookStatus.occupiedOnAudit().contains(BookStatus.PENDING),
                "审核时把待审核算作占用，会导致两条重叠的待审记录谁都无法通过");
    }

    @Test
    @DisplayName("状态集合不可被调用方修改")
    void statusSetsAreImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> BookStatus.occupiedOnSubmit().add(BookStatus.CANCELLED));
        assertThrows(UnsupportedOperationException.class,
                () -> BookStatus.occupiedOnAudit().add(BookStatus.PENDING));
    }

    @Test
    @DisplayName("只有待审核与已通过可以取消")
    void cancellableStatus() {
        assertTrue(BookStatus.cancellable(BookStatus.PENDING));
        assertTrue(BookStatus.cancellable(BookStatus.APPROVED));
        assertFalse(BookStatus.cancellable(BookStatus.REJECTED));
        assertFalse(BookStatus.cancellable(BookStatus.CANCELLED));
        assertFalse(BookStatus.cancellable(BookStatus.COMPLETED));
        assertFalse(BookStatus.cancellable(null));
    }

    @Test
    @DisplayName("审核结果只能是已通过或已驳回")
    void auditResult() {
        assertTrue(BookStatus.isAuditResult(BookStatus.APPROVED));
        assertTrue(BookStatus.isAuditResult(BookStatus.REJECTED));
        assertFalse(BookStatus.isAuditResult(BookStatus.PENDING));
        assertFalse(BookStatus.isAuditResult(BookStatus.CANCELLED));
        assertFalse(BookStatus.isAuditResult(null));
    }

    @Test
    @DisplayName("状态码转中文，未知值与 null 不抛异常")
    void statusText() {
        assertEquals("待审核", BookStatus.text(BookStatus.PENDING));
        assertEquals("已通过", BookStatus.text(BookStatus.APPROVED));
        assertEquals("已驳回", BookStatus.text(BookStatus.REJECTED));
        assertEquals("已取消", BookStatus.text(BookStatus.CANCELLED));
        assertEquals("已完成", BookStatus.text(BookStatus.COMPLETED));
        assertEquals("未知", BookStatus.text(99));
        assertEquals("未知", BookStatus.text(null));
    }
}
