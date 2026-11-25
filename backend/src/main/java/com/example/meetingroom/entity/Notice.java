package com.example.meetingroom.entity;

import java.time.LocalDateTime;

/**
 * 公告，对应 notice 表，字段与表列一一对应。
 */
public class Notice {

    /** 主键 */
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 发布人 */
    private Long publisherId;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 0 不展示，1 首页展示 */
    private Integer isShow;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Long publisherId) {
        this.publisherId = publisherId;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public Integer getIsShow() {
        return isShow;
    }

    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }
}
