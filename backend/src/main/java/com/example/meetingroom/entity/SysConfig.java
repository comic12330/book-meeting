package com.example.meetingroom.entity;

import java.time.LocalDateTime;

/**
 * 系统业务参数，对应 sys_config 表。
 * 一行一个参数，键值均为字符串，读取时按 {@link com.example.meetingroom.common.SysConfigKey} 声明的类型转换。
 */
public class SysConfig {

    /** 参数键，主键 */
    private String configKey;

    /** 参数值 */
    private String configValue;

    /** 参数名称 */
    private String configName;

    /** 单位，无单位时为空串 */
    private String unit;

    /** 说明 */
    private String remark;

    /** 排序号 */
    private Integer sortNo;

    /** 最后修改时间 */
    private LocalDateTime updateTime;

    /*
     * 以下三个字段不是表列，由 SysConfigService 读取时按 SysConfigKey 枚举回填，
     * 供管理页面决定输入控件类型与取值范围，避免前端再硬编码一遍规则。
     */

    /** 值类型：INT 或 TIME */
    private String valueType;

    /** 整数型的最小值 */
    private Integer minValue;

    /** 整数型的最大值 */
    private Integer maxValue;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }
}
