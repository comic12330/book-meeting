package com.example.meetingroom.controller.admin;

import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.Result;
import com.example.meetingroom.dto.ResetPasswordDTO;
import com.example.meetingroom.dto.UserQueryDTO;
import com.example.meetingroom.dto.UserSaveDTO;
import com.example.meetingroom.entity.User;
import com.example.meetingroom.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 管理端用户管理：列表、新增、编辑、启用/停用、重置密码。
 * 路径位于 /api/admin/** 下，管理员身份由 AuthInterceptor 统一校验。
 * 不提供物理删除——user 表被 meeting_book.user_id 引用，删人会破坏历史预定记录，需要「移除」时改为停用。
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Resource
    private UserService userService;

    /**
     * 分页查询用户，支持按账号、姓名、角色与状态筛选。
     * 返回前已把每条记录的密码字段置空。
     */
    @GetMapping
    public Result<PageResult<User>> page(UserQueryDTO query) {
        return Result.success(userService.pageForAdmin(query));
    }

    /**
     * 新建账号。角色与初始密码由管理员指定，不设固定默认密码。
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid UserSaveDTO dto) {
        return Result.success("用户已新增", userService.createByAdmin(dto));
    }

    /**
     * 编辑账号资料。两条防呆：不能停用当前登录账号，不能把最后一个管理员降级。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid UserSaveDTO dto) {
        userService.updateByAdmin(id, dto);
        return Result.success("用户信息已更新", null);
    }

    /**
     * 启用或停用账号。
     * 停用后该账号的令牌立刻失效——拦截器每次请求都会回查数据库状态，不是等令牌自然过期才生效。
     */
    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        userService.updateStatusByAdmin(id, status);
        return Result.success(Integer.valueOf(1).equals(status) ? "账号已启用" : "账号已停用", null);
    }

    /**
     * 重置他人密码。不校验原密码（执行者身份已由拦截器校验），新密码由管理员设定。
     */
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody @Valid ResetPasswordDTO dto) {
        userService.resetPasswordByAdmin(id, dto);
        return Result.success("密码已重置", null);
    }
}
