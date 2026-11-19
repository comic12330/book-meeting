package com.example.meetingroom.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 部署级参数，来自 application.yml 的 meeting.page 段。
 * <p>
 * 分页上下限属于技术保护措施（防止前端传 pageSize=999999 一次拉全表），
 * 与业务运营无关，因此放在配置文件由部署方掌握，不暴露给管理员在线修改。
 * <p>
 * {@link PageQuery} 是被 Spring 直接绑定参数的普通 DTO，不是容器管理的 Bean，
 * 无法注入配置；这里用静态字段做一次桥接：容器启动时由 setter 把配置写进静态字段，
 * 之后 PageQuery 直接读静态方法即可。
 */
@Component
public class AppProperties {

    private static int defaultPageSize = 10;

    private static int maxPageSize = 100;

    @Value("${meeting.page.default-size:10}")
    public void setDefaultPageSize(int value) {
        AppProperties.defaultPageSize = value;
    }

    @Value("${meeting.page.max-size:100}")
    public void setMaxPageSize(int value) {
        AppProperties.maxPageSize = value;
    }

    public static int defaultPageSize() {
        return defaultPageSize;
    }

    public static int maxPageSize() {
        return maxPageSize;
    }
}
