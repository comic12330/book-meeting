package com.example.meetingroom.controller.admin;

import com.example.meetingroom.common.Result;
import com.example.meetingroom.dto.ConfigSaveDTO;
import com.example.meetingroom.entity.SysConfig;
import com.example.meetingroom.service.SysConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 管理端业务参数维护。
 * <p>
 * 只提供「查看」与「改值」两个动作：参数项本身由初始化脚本写入，
 * 键名是代码里声明过的契约（{@link com.example.meetingroom.common.SysConfigKey}），
 * 所以页面上不能新增或删除参数项，避免出现代码读不到的多余配置。
 */
@RestController
@RequestMapping("/api/admin/configs")
public class AdminConfigController {

    @Resource
    private SysConfigService configService;

    @GetMapping
    public Result<List<SysConfig>> list() {
        return Result.success(configService.list());
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid ConfigSaveDTO dto) {
        configService.update(dto.getConfigKey(), dto.getConfigValue());
        return Result.success("参数已保存，即时生效", null);
    }
}
