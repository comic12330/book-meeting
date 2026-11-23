package com.example.meetingroom.controller;

import com.example.meetingroom.common.Result;
import com.example.meetingroom.service.SysConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 面向已登录用户的参数读取接口。
 *
 * 前端要用可预定窗口与人数上限来限制日期选择器和数字输入框，
 * 避免用户填完才被后端拒绝。这些值不涉及敏感信息，登录用户均可读取；
 * 真正的校验仍在服务端执行，前端限制只是体验优化。
 */
@RestController
@RequestMapping("/api/configs")
public class ConfigController {

    @Resource
    private SysConfigService configService;

    @GetMapping
    public Result<Map<String, String>> configs() {
        return Result.success(configService.publicMap());
    }
}
