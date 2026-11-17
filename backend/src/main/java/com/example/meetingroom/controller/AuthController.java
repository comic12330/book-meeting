package com.example.meetingroom.controller;

import com.example.meetingroom.common.Result;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.dto.LoginDTO;
import com.example.meetingroom.dto.RegisterDTO;
import com.example.meetingroom.entity.User;
import com.example.meetingroom.service.UserService;
import com.example.meetingroom.vo.LoginVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success("登录成功", userService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功，请登录", null);
    }

    @GetMapping("/me")
    public Result<User> me() {
        return Result.success(userService.getProfile(UserContext.userId()));
    }
}
