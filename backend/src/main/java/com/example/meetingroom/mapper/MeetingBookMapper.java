package com.example.meetingroom.mapper;

import com.example.meetingroom.entity.MeetingBook;
import com.example.meetingroom.vo.BookVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预定表的数据访问接口，SQL 语句写在 MeetingBookMapper.xml 中。
 */
public interface MeetingBookMapper {

    /**
     * 冲突计数：同一会议室下存在指定状态的记录，且「已有开始 &lt; 新结束 且 已有结束 &gt; 新开始」。
     * statuses 由调用方决定参与判定的状态集合：提交环节传 [0,1]（待审核 + 已通过），审核环节只传 [1]（已通过）。
     * 审核时不把待审核算作占用，否则两条重叠的待审记录会互相阻断、永远无法通过。
     *
     * @param excludeId 审核场景下排除记录自身，提交场景传 null
     */
    int countConflict(@Param("roomId") Long roomId,
                      @Param("startTime") LocalDateTime startTime,
                      @Param("endTime") LocalDateTime endTime,
                      @Param("excludeId") Long excludeId,
                      @Param("statuses") List<Integer> statuses);

    /** 新增预定，回填自增主键 */
    int insert(MeetingBook book);

    /** 按主键取预定记录，只含表字段，不带会议室与申请人信息 */
    MeetingBook findById(@Param("id") Long id);

    /** 按主键取详情，额外带会议室名称、位置与申请人姓名，供详情接口使用 */
    BookVO findVOById(@Param("id") Long id);

    /** 用户端「我的预定」：按申请人查询，状态为空表示全部 */
    List<BookVO> selectMyBooks(@Param("userId") Long userId, @Param("status") Integer status);

    /** 管理端预定列表：按状态、会议室与关键字筛选，关键字匹配申请人或会议用途 */
    List<BookVO> selectForAdmin(@Param("status") Integer status,
                                @Param("roomId") Long roomId,
                                @Param("keyword") String keyword);

    /** 统计某会议室被预定过的条数，删除会议室前用它判断是否存在历史引用 */
    int countByRoom(@Param("roomId") Long roomId);

    /** 审核结果写回：状态、审核意见与审核时间一起更新 */
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("auditMsg") String auditMsg,
                     @Param("auditTime") LocalDateTime auditTime);

    /**
     * 取消预定：只写状态、取消理由与取消时间，不动 audit_msg / audit_time。
     * 与 updateStatus 分开是必需的：取消若复用审核语句，会把已有的审核痕迹清空，「审核通过 → 取消」的记录就再也看不出何时被审批过。同样的理由让 completeExpired也没有复用 updateStatus。
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
     * 只更新 status 一列，不动 audit_msg / audit_time（与 updateStatus 的区别详见 MeetingBookMapper.xml 中的说明）。时间比较交给数据库的 NOW() 完成。
     *
     * @param fromStatus 源状态，业务上固定传「已通过」
     * @param toStatus   目标状态，业务上固定传「已完成」
     * @return 本次实际更新的行数，为 0 表示没有待处理的记录
     */
    int completeExpired(@Param("fromStatus") Integer fromStatus,
                        @Param("toStatus") Integer toStatus);
}
