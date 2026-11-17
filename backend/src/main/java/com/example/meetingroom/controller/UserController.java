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

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/profile")
    public Result<User> profile() {
        return Result.success(userService.getProfile(UserContext.userId()));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User form) {
        userService.updateProfile(UserContext.userId(), form);
        return Result.success("资料已更新", null);
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid PasswordDTO dto) {
        userService.changePassword(UserContext.userId(), dto);
        return Result.success("密码已修改，请重新登录", null);
    }
}
