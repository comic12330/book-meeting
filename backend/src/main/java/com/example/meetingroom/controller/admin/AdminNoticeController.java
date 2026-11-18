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
 * 管理端公告维护：新增、编辑、删除、查看全部。
 * <p>
 * 与首页的 NoticeController 区别在于这里返回全部记录（含 is_show = 0 的），
 * 并把展示中的排在前面，便于管理员区分「正在展示」与「已下架留档」。
 */
@RestController
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 查询全部公告，含已隐藏的，按展示优先 + 发布时间倒序。
     */
    @GetMapping
    public Result<List<Notice>> list() {
        return Result.success(noticeService.listAll());
    }

    /**
     * 发布公告。发布人取当前登录的管理员，不从请求体接收。
     */
    @PostMapping
    public Result<Long> save(@RequestBody Notice notice) {
        return Result.success("公告已发布", noticeService.save(notice));
    }

    /**
     * 编辑公告。只更新标题、内容与展示状态；想下架但保留内容，把 isShow 置 0 即可。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Notice notice) {
        noticeService.update(id, notice);
        return Result.success("公告已更新", null);
    }

    /**
     * 删除公告。公告不参与业务判定，可以物理删除。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success("公告已删除", null);
    }
}
