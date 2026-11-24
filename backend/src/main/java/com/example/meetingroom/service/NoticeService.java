package com.example.meetingroom.service;

import com.example.meetingroom.annotation.LogOperation;
import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.OperationType;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.entity.Notice;
import com.example.meetingroom.mapper.NoticeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 公告管理。
 *
 * 公告有两个可见范围：首页只取 is_show = 1 的记录（{@link #listShow}），管理端取全部（{@link #listAll}），过期的公告可以置为隐藏留档而不必删除。
 * 首页取数时在 SQL 层就过滤掉隐藏记录，不依赖前端判断，避免把不该公开的内容传到浏览器。
 */
@Service
public class NoticeService {

    @Resource
    private NoticeMapper noticeMapper;

    /**
     * 首页用：只返回首页展示的公告，按发布时间倒序。
     */
    public List<Notice> listShow() {
        return noticeMapper.selectShow();
    }

    /**
     * 管理端用：返回全部公告，含已隐藏的，按「展示优先 + 发布时间倒序」排列，便于管理员看到哪些公告已经下架留档。
     */
    public List<Notice> listAll() {
        return noticeMapper.selectAll();
    }

    /**
     * 发布公告。标题与内容必填，发布人取当前登录用户，不从表单接收。
     * isShow 未指定时默认 1，即发布后立即在首页展示。
     *
     * @return 新公告的主键
     */
    @Transactional(rollbackFor = Exception.class)
    @LogOperation(type = OperationType.NOTICE_CREATE, target = "#notice.title")
    public Long save(Notice notice) {
        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "请填写公告标题");
        }
        if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "请填写公告内容");
        }
        notice.setTitle(notice.getTitle().trim());
        notice.setPublisherId(UserContext.userId());
        notice.setIsShow(notice.getIsShow() == null ? 1 : notice.getIsShow());
        noticeMapper.insert(notice);
        return notice.getId();
    }

    /**
     * 编辑公告。发布人与发布时间不在可改范围内，只更新标题、内容和展示状态。
     */
    @Transactional(rollbackFor = Exception.class)
    @LogOperation(type = OperationType.NOTICE_UPDATE, target = "#notice.title")
    public void update(Long id, Notice notice) {
        if (noticeMapper.findById(id) == null) {
            throw new BusinessException(1001, "公告不存在");
        }
        notice.setId(id);
        notice.setTitle(notice.getTitle() == null ? null : notice.getTitle().trim());
        notice.setIsShow(notice.getIsShow() == null ? 1 : notice.getIsShow());
        noticeMapper.update(notice);
    }

    /**
     * 删除公告。公告不参与任何业务判定，可以物理删除；若只是想从首页撤下但保留内容，应编辑并把 isShow 置为 0，而不是删除。
     */
    @Transactional(rollbackFor = Exception.class)
    @LogOperation(type = OperationType.NOTICE_DELETE, target = "describe(#id)")
    public void delete(Long id) {
        if (noticeMapper.findById(id) == null) {
            throw new BusinessException(1001, "公告不存在");
        }
        noticeMapper.deleteById(id);
    }

    /**
     * 供操作日志取对象的可读描述，返回公告标题。
     * 由切面的 SpEL 调用（见 {@code @LogOperation(target = "describe(#id)")}），删除只拿到主键，光记 id 事后看不出删的是哪条公告。查不到时返回 null，切面会忽略。
     */
    public String describe(Long id) {
        Notice notice = noticeMapper.findById(id);
        return notice == null ? null : notice.getTitle();
    }
}
