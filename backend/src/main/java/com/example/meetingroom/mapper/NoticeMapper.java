package com.example.meetingroom.mapper;

import com.example.meetingroom.entity.Notice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoticeMapper {

    List<Notice> selectShow();

    List<Notice> selectAll();

    Notice findById(@Param("id") Long id);

    int insert(Notice notice);

    int update(Notice notice);

    int deleteById(@Param("id") Long id);
}
