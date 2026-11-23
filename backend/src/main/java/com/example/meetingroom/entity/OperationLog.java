package com.example.meetingroom.entity;

import java.time.LocalDateTime;

/**
 * 操作日志。
 *
 * 三个必留要素：操作人（operatorId + operatorName 快照）、操作类型（module + opType）、
 * 操作时间（opTime）。另外记录结果与失败原因，因为被拒绝的越权尝试比成功流水更有追查价值。
 * 本对象只在写入与查询时使用，没有任何更新入口，审计日志可以被改就等于没有。
 */
public class OperationLog {

    /** 主键 */
    private Long id;

    /** 操作人主键 */
    private Long operatorId;

    /** 操作人姓名，存操作当时的快照，不随用户改名而变化 */
    private String operatorName;

    /** 所属模块：ROOM / BOOK / USER / NOTICE / CONFIG */
    private String module;

    /** 操作类型 code，与 OperationType 枚举一一对应 */
    private String opType;

    /** 结果：1 成功，0 失败 */
    private Integer opResult;

    /** 失败原因 */
    private String resultMsg;

    /** 操作对象的可读描述 */
    private String targetDesc;

    /** 操作时间 */
    private LocalDateTime opTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

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

    public Integer getOpResult() {
        return opResult;
    }

    public void setOpResult(Integer opResult) {
        this.opResult = opResult;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getTargetDesc() {
        return targetDesc;
    }

    public void setTargetDesc(String targetDesc) {
        this.targetDesc = targetDesc;
    }

    public LocalDateTime getOpTime() {
        return opTime;
    }

    public void setOpTime(LocalDateTime opTime) {
        this.opTime = opTime;
    }
}
