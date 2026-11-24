package com.example.meetingroom.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 预定状态字典。
 *
 * 这些取值对应 meeting_book.status 列，同时被 SQL 的 IN 条件、审核流程与前端展示共同使用。
 * 之所以抽成常量而不是放进配置文件：状态码一旦改动，库里全部历史记录的语义都会跟着变，它属于代码与数据共同约定的字典，不是运营可以随手调整的参数，这里的常量类只承担消除魔法数字、集中定义的职责。
 * 两个「参与冲突判定的状态集合」也定义在这里，因为它们是状态码的直接派生，分开写容易出现「改了状态码忘了改集合」的隐性问题。
 */
public final class BookStatus {

    /** 待审核 */
    public static final int PENDING = 0;

    /** 审核通过 */
    public static final int APPROVED = 1;

    /** 审核驳回 */
    public static final int REJECTED = 2;

    /** 已取消 */
    public static final int CANCELLED = 3;

    /** 已完成 */
    public static final int COMPLETED = 4;

    /**
     * 提交环节参与冲突判定的状态：待审核与已通过都算占用。
     * 待审核也算，是为了让进入待审队列的申请互不冲突，避免同一时段被反复提交。
     */
    private static final List<Integer> OCCUPIED_ON_SUBMIT =
            Collections.unmodifiableList(Arrays.asList(PENDING, APPROVED));

    /**
     * 审核环节参与冲突判定的状态：只有已通过才算真正占用。
     * 若把待审核也算进来，两条时段重叠的待审记录会互相阻断，谁都无法通过。
     */
    private static final List<Integer> OCCUPIED_ON_AUDIT =
            Collections.singletonList(APPROVED);

    private BookStatus() {
    }

    public static List<Integer> occupiedOnSubmit() {
        return OCCUPIED_ON_SUBMIT;
    }

    public static List<Integer> occupiedOnAudit() {
        return OCCUPIED_ON_AUDIT;
    }

    /** 状态码转中文，用于拼装提示语 */
    public static String text(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case PENDING:
                return "待审核";
            case APPROVED:
                return "已通过";
            case REJECTED:
                return "已驳回";
            case CANCELLED:
                return "已取消";
            case COMPLETED:
                return "已完成";
            default:
                return "未知";
        }
    }

    /** 是否为可取消的状态（待审核、已通过） */
    public static boolean cancellable(Integer status) {
        return Integer.valueOf(PENDING).equals(status) || Integer.valueOf(APPROVED).equals(status);
    }

    /** 是否为审核结果可选值（只能是通过或驳回） */
    public static boolean isAuditResult(Integer status) {
        return Integer.valueOf(APPROVED).equals(status) || Integer.valueOf(REJECTED).equals(status);
    }
}
