package com.example.meetingroom.dto;

import com.example.meetingroom.common.PageQuery;

import java.time.LocalDateTime;

/**
 * 操作日志查询条件。
 * <p>
 * 五个条件都是可选的，组合使用即「某模块下某人在某段时间做过什么」。
 * 时间范围用左闭右闭，前端传当天 00:00:00 至 23:59:59 就能覆盖整天。
 */
public class LogQueryDTO extends PageQuery {

    /** 模块：ROOM / BOOK / USER / NOTICE / CONFIG */
    private String module;

    /** 操作类型 code */
    private String opType;

    /** 操作人关键字，匹配姓名或账号对应的记录（按姓名快照模糊匹配） */
    private String operator;

    /** 起始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
