package com.example.meetingroom.mapper;

import com.example.meetingroom.entity.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告表的数据访问接口，SQL 语句写在 NoticeMapper.xml 中。
 */
public interface NoticeMapper {

    /** 首页用：只取 is_show = 1 的公告 */
    List<Notice> selectShow();

    /** 管理端用：取全部公告，展示中的排在前面 */
    List<Notice> selectAll();

    /** 编辑、删除前的存在性判断 */
    Notice findById(@Param("id") Long id);

    /** 新增公告，回填自增主键 */
    int insert(Notice notice);

    /** 只更新标题、内容与展示状态，发布人和发布时间不变 */
    int update(Notice notice);

    /** 物理删除，公告不参与业务判定，可以真删 */
    int deleteById(@Param("id") Long id);
}
