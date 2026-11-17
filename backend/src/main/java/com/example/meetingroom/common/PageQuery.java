package com.example.meetingroom.common;

/**
 * 分页查询基类，供各查询 DTO 继承。
 */
public class PageQuery {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    /** 防止前端传非法页码导致 PageHelper 异常 */
    public int safePageNum() {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    /** 单页上限 100，避免一次拉全表 */
    public int safePageSize() {
        int size = pageSize == null ? 10 : pageSize;
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
