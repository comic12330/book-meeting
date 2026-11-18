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

/**
 * 登录认证接口。
 * <p>
 * 三个接口的鉴权范围不同：
 * <ul>
 *   <li>/login 与 /register 在 WebConfig 中被排除在拦截器之外，允许匿名访问；</li>
 *   <li>/me 需要携带令牌，前端刷新页面后靠它恢复登录态与角色。</li>
 * </ul>
 * 密码比对、停用判断等规则都在 UserService 内，Controller 只负责组织响应。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private UserService userService;

    /**
     * 账号密码登录，成功后返回令牌与用户基本信息。
     * 账号不存在与密码错误返回同一句提示，避免被用来探测账号是否存在。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success("登录成功", userService.login(dto));
    }

    /**
     * 自助注册。角色固定为普通用户、状态固定为可用，两者都不接受前端指定，
     * 否则可以构造请求把自己注册成管理员。
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功，请登录", null);
    }

    /**
     * 取当前登录用户资料。用户 ID 来自令牌解析结果，不从请求参数接收；
     * 返回体中的密码字段已被置空。
     */
    @GetMapping("/me")
    public Result<User> me() {
        return Result.success(userService.getProfile(UserContext.userId()));
    }
}
