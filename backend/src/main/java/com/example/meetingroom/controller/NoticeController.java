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
 * 首页公告（仅返回 is_show = 1 的记录）。
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @GetMapping
    public Result<List<Notice>> list() {
        return Result.success(noticeService.listShow());
    }
}
