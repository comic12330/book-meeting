package com.example.meetingroom.service;

import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.entity.Notice;
import com.example.meetingroom.mapper.NoticeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 公告管理。
 */
@Service
public class NoticeService {

    @Resource
    private NoticeMapper noticeMapper;

    public List<Notice> listShow() {
        return noticeMapper.selectShow();
    }

    public List<Notice> listAll() {
        return noticeMapper.selectAll();
    }

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, Notice notice) {
        if (noticeMapper.findById(id) == null) {
            throw new BusinessException(1001, "公告不存在");
        }
        notice.setId(id);
        notice.setTitle(notice.getTitle() == null ? null : notice.getTitle().trim());
        notice.setIsShow(notice.getIsShow() == null ? 1 : notice.getIsShow());
        noticeMapper.update(notice);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (noticeMapper.findById(id) == null) {
            throw new BusinessException(1001, "公告不存在");
        }
        noticeMapper.deleteById(id);
    }
}
