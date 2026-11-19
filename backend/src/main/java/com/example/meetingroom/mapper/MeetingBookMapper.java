package com.example.meetingroom.mapper;

import com.example.meetingroom.entity.MeetingBook;
import com.example.meetingroom.vo.BookVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingBookMapper {

    /**
     * 冲突计数：同一会议室下存在指定状态的记录，且「已有开始 &lt; 新结束 且 已有结束 &gt; 新开始」。
     * <p>
     * statuses 由调用方决定参与判定的状态集合：
     * 提交环节传 [0,1]（待审核 + 已通过），审核环节只传 [1]（已通过）。
     * 审核时不把「待审核」算作占用，否则两条重叠的待审记录会互相阻断、永远无法通过。
     *
     * @param excludeId 审核场景下排除记录自身，提交场景传 null
     */
    int countConflict(@Param("roomId") Long roomId,
                      @Param("startTime") LocalDateTime startTime,
                      @Param("endTime") LocalDateTime endTime,
                      @Param("excludeId") Long excludeId,
                      @Param("statuses") List<Integer> statuses);

    int insert(MeetingBook book);

    MeetingBook findById(@Param("id") Long id);

    BookVO findVOById(@Param("id") Long id);

    List<BookVO> selectMyBooks(@Param("userId") Long userId, @Param("status") Integer status);

    List<BookVO> selectForAdmin(@Param("status") Integer status,
                                @Param("roomId") Long roomId,
                                @Param("keyword") String keyword);

    int countByRoom(@Param("roomId") Long roomId);

    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("auditMsg") String auditMsg,
                     @Param("auditTime") LocalDateTime auditTime);

    /**
     * 取消预定：只写状态、取消理由与取消时间，不动 audit_msg / audit_time。
     * <p>
     * 与 updateStatus 分开是必需的——取消若复用审核语句，会把已有的审核痕迹清空，
     * 「审核通过 → 取消」的记录就再也看不出何时被审批过。同样的理由让 completeExpired
     * 也没有复用 updateStatus。
     *
     * @param cancelReason 管理员取消他人预定时必填，本人取消传 null
     * @param cancelTime   取消时间，由服务层取当前时间
     */
    int cancelById(@Param("id") Long id,
                   @Param("status") Integer status,
                   @Param("cancelReason") String cancelReason,
                   @Param("cancelTime") LocalDateTime cancelTime);

    /**
     * 定时任务专用：把使用时间已结束的预定批量置为已完成。
     * <p>
     * 只更新 status 一列，不动 audit_msg / audit_time（这与 updateStatus 的区别，
     * 详见 MeetingBookMapper.xml 中的说明）。时间比较交给数据库的 NOW() 完成。
     *
     * @param fromStatus 源状态，业务上固定传「已通过」
     * @param toStatus   目标状态，业务上固定传「已完成」
     * @return 本次实际更新的行数，为 0 表示没有待处理的记录
     */
    int completeExpired(@Param("fromStatus") Integer fromStatus,
                        @Param("toStatus") Integer toStatus);
}
