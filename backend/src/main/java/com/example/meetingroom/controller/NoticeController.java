package com.example.meetingroom.controller;

import com.example.meetingroom.common.Result;
import com.example.meetingroom.entity.Notice;
import com.example.meetingroom.service.NoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 首页公告查询。
 * <p>
 * 只返回 is_show = 1 的记录，过滤在 SQL 层完成，隐藏的公告不会出现在响应体里，
 * 而不是先全量返回再由前端隐藏。
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 首页公告列表，按发布时间倒序。
     */
    @GetMapping
    public Result<List<Notice>> list() {
        return Result.success(noticeService.listShow());
    }
}
