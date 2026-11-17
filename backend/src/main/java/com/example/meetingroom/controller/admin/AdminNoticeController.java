package com.example.meetingroom.controller.admin;

import com.example.meetingroom.common.Result;
import com.example.meetingroom.entity.Notice;
import com.example.meetingroom.service.NoticeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理端公告维护。
 */
@RestController
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

    @Resource
    private NoticeService noticeService;

    @GetMapping
    public Result<List<Notice>> list() {
        return Result.success(noticeService.listAll());
    }

    @PostMapping
    public Result<Long> save(@RequestBody Notice notice) {
        return Result.success("公告已发布", noticeService.save(notice));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Notice notice) {
        noticeService.update(id, notice);
        return Result.success("公告已更新", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success("公告已删除", null);
    }
}
