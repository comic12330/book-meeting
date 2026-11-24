package com.example.meetingroom.controller.admin;

import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.Result;
import com.example.meetingroom.dto.LogQueryDTO;
import com.example.meetingroom.entity.OperationLog;
import com.example.meetingroom.service.OperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端操作日志查询。
 *
 * 只有查询，没有修改与删除接口：审计日志能被改就等于没有审计，所以这里刻意不提供任何写操作，管理员本人想删也删不掉。
 * 管理员身份由 AuthInterceptor 按路径前缀 /api/admin/ 统一校验，本类方法体内不再重复判断角色。
 */
@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {

    @Resource
    private OperationLogService logService;

    /**
     * 分页查询，支持按模块、操作类型、操作人与时间范围筛选，按操作时间倒序。
     * 普通用户没有这个入口，也调不通这个接口。
     */
    @GetMapping
    public Result<PageResult<OperationLog>> page(LogQueryDTO query) {
        return Result.success(logService.page(query));
    }
}
