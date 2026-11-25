package com.example.meetingroom.dto;

import javax.validation.constraints.NotBlank;

/**
 * 修改业务参数的请求体。
 * 参数键与值一并放在请求体里传，而不是把键放到 URL 路径上——键名形如 booking.max-hours 带点号，放路径容易被后缀解析规则截断。
 */
public class ConfigSaveDTO {

    /** 参数键，如 booking.max-hours */
    @NotBlank(message = "请指定参数项")
    private String configKey;

    /** 新的参数值，按该参数的取值范围校验 */
    @NotBlank(message = "参数值不能为空")
    private String configValue;

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
}
