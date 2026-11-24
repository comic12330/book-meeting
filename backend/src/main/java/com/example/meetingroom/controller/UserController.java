package com.example.meetingroom.controller;

import com.example.meetingroom.common.Result;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.dto.PasswordDTO;
import com.example.meetingroom.entity.User;
import com.example.meetingroom.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 个人中心：查看资料、修改资料、修改密码。
 *
 * 三个接口都只作用于当前登录用户，用户 ID 来自令牌解析结果（UserContext），不接受前端传入 ID，因此不存在改到别人资料的可能。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 查看本人资料，返回前已清除密码字段。
     */
    @GetMapping("/profile")
    public Result<User> profile() {
        return Result.success(userService.getProfile(UserContext.userId()));
    }

    /**
     * 修改本人资料。服务层只取真实姓名与手机号两个字段，请求体里即使带上 role、status 也会被忽略。
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User form) {
        userService.updateProfile(UserContext.userId(), form);
        return Result.success("资料已更新", null);
    }

    /**
     * 修改本人密码，必须提供正确的原密码。
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid PasswordDTO dto) {
        userService.changePassword(UserContext.userId(), dto);
        return Result.success("密码已修改，请重新登录", null);
    }
}
