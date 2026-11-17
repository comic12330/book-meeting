package com.example.meetingroom.service;

import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.dto.LoginDTO;
import com.example.meetingroom.dto.PasswordDTO;
import com.example.meetingroom.dto.RegisterDTO;
import com.example.meetingroom.entity.User;
import com.example.meetingroom.mapper.UserMapper;
import com.example.meetingroom.util.Md5Util;
import com.example.meetingroom.util.TokenUtil;
import com.example.meetingroom.vo.LoginVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 用户与登录认证。
 */
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TokenUtil tokenUtil;

    public LoginVO login(LoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername().trim());
        if (user == null || !Md5Util.verify(dto.getPassword(), user.getPassword())) {
            // 账号不存在与密码错误返回同一提示，避免被用来探测账号是否存在
            throw new BusinessException(1001, "账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(1001, "账号已被禁用，请联系管理员");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(tokenUtil.create(user.getId(), user.getUsername(), user.getRole()));
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        String username = dto.getUsername().trim();
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(1001, "该账号已被注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.encode(dto.getPassword()));
        user.setRealName(dto.getRealName().trim());
        user.setPhone(dto.getPhone());
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);
    }

    public User getProfile(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, User form) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        if (form.getRealName() == null || form.getRealName().trim().isEmpty()) {
            throw new BusinessException(400, "真实姓名不能为空");
        }
        User update = new User();
        update.setId(userId);
        update.setRealName(form.getRealName().trim());
        update.setPhone(form.getPhone());
        userMapper.updateProfile(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, PasswordDTO dto) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        if (!Md5Util.verify(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(1001, "原密码不正确");
        }
        userMapper.updatePassword(userId, Md5Util.encode(dto.getNewPassword()));
    }
}
