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
}
