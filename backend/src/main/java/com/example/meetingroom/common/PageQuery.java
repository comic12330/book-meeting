package com.example.meetingroom.common;

/**
 * 分页查询基类，供各查询 DTO 继承。
 *
 * 单页条数的上限与默认值取自 application.yml 的 meeting.page 段（见 {@link AppProperties}），不再写死在本类里。
 * 上限的作用是防止前端传一个极大值把整表拉出来。
 */
public class PageQuery {

    private Integer pageNum = 1;

    private Integer pageSize;

    /** 防止前端传非法页码导致 PageHelper 异常 */
    public int safePageNum() {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    /** 单页条数：为空取配置默认值，超上限按上限截断 */
    public int safePageSize() {
        int size = pageSize == null ? AppProperties.defaultPageSize() : pageSize;
        if (size < 1) {
            return AppProperties.defaultPageSize();
        }
        return Math.min(size, AppProperties.maxPageSize());
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize == null ? AppProperties.defaultPageSize() : pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
