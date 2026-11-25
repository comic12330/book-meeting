package com.example.meetingroom.common;

import java.util.List;

/**
 * 分页返回结构。
 */
public class PageResult<T> {

    /** 符合条件的总条数，前端用来算总页数 */
    private long total;

    /** 当前页的数据 */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(long total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    /** 按总条数与当前页数据构造分页结果 */
    public static <T> PageResult<T> of(long total, List<T> list) {
        return new PageResult<>(total, list);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
