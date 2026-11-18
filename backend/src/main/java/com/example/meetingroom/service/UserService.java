package com.example.meetingroom.service;

import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.dto.LoginDTO;
import com.example.meetingroom.dto.PasswordDTO;
import com.example.meetingroom.dto.RegisterDTO;
import com.example.meetingroom.dto.ResetPasswordDTO;
import com.example.meetingroom.dto.UserQueryDTO;
import com.example.meetingroom.dto.UserSaveDTO;
import com.example.meetingroom.entity.User;
import com.example.meetingroom.mapper.UserMapper;
import com.example.meetingroom.util.Md5Util;
import com.example.meetingroom.util.TokenUtil;
import com.example.meetingroom.vo.LoginVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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

    // ==================== 管理端用户管理 ====================

    public PageResult<User> pageForAdmin(UserQueryDTO query) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<User> list = userMapper.selectByCondition(query);
        // 摘要不出库，列表同样置空后再返回
        list.forEach(item -> item.setPassword(null));
        PageInfo<User> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getTotal(), list);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createByAdmin(UserSaveDTO dto) {
        String username = dto.getUsername().trim();
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(1001, "该账号已存在");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new BusinessException(400, "请为新账号设置初始密码");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.encode(dto.getPassword()));
        user.setRealName(dto.getRealName().trim());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        userMapper.insert(user);
        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateByAdmin(Long id, UserSaveDTO dto) {
        User exist = requireUser(id);
        // 允许改自己的资料，但不能在编辑里顺手把自己停用，否则当场失去入口
        if (id.equals(UserContext.userId()) && !Integer.valueOf(1).equals(dto.getStatus())) {
            throw new BusinessException(1001, "不能停用当前登录的账号");
        }
        // 降级是可以让系统失去管理入口的唯一路径：停用自己已被上一句拦住，
        // 而唯一的管理员只能是自己，所以「最后一名管理员」的判定实际只在降级时生效。
        boolean demote = Integer.valueOf(1).equals(exist.getRole()) && !Integer.valueOf(1).equals(dto.getRole());
        if (demote) {
            ensureNotLastAdmin(id, "该账号是当前唯一启用的管理员，不能改为普通用户");
        }

        User update = new User();
        update.setId(id);
        update.setRealName(dto.getRealName().trim());
        update.setPhone(dto.getPhone());
        update.setRole(dto.getRole());
        update.setStatus(dto.getStatus());
        userMapper.updateByAdmin(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatusByAdmin(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "账号状态只能为 0（禁用）或 1（可用）");
        }
        requireUser(id);
        if (status == 0 && id.equals(UserContext.userId())) {
            throw new BusinessException(1001, "不能停用当前登录的账号");
        }
        userMapper.updateStatus(id, status);
    }

    /**
     * 管理员重置他人口令：不校验原密码，执行者身份已由 /api/admin/** 的拦截器校验。
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByAdmin(Long id, ResetPasswordDTO dto) {
        requireUser(id);
        userMapper.updatePassword(id, Md5Util.encode(dto.getNewPassword()));
    }

    private User requireUser(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        return user;
    }

    /** 系统至少要留一个启用的管理员，否则管理端再也进不去 */
    private void ensureNotLastAdmin(Long excludeId, String message) {
        if (userMapper.countOtherActiveAdmin(excludeId) == 0) {
            throw new BusinessException(1001, message);
        }
    }
}
