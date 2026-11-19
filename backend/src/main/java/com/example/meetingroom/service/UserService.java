package com.example.meetingroom.service;

import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.PageResult;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.common.UserRole;
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
 * <p>
 * 分两块职责：
 * <ul>
 *   <li><b>认证与自助</b>：登录、注册、查看与修改个人资料、修改密码；</li>
 *   <li><b>管理端用户管理</b>：新建账号、编辑、启用停用、重置密码。</li>
 * </ul>
 * 两条贯穿全类的规则：
 * <ol>
 *   <li>密码一律 MD5 加密后入库，任何返回给前端的对象都要把 password 置空；</li>
 *   <li>不允许删除用户，只能停用——预定表通过外键引用用户，删人会让历史预定失去归属。</li>
 * </ol>
 */
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TokenUtil tokenUtil;

    /**
     * 登录校验，成功后签发令牌。
     * <p>
     * 校验顺序：账号密码 → 账号是否被停用。密码用 MD5 比对，库里不存明文。
     *
     * @return 令牌与用户基本信息的组合，前端保存令牌并在后续请求的请求头中携带
     * @throws BusinessException 账号不存在、密码错误或账号已停用时抛出
     */
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.findByUsername(dto.getUsername().trim());
        if (user == null || !Md5Util.verify(dto.getPassword(), user.getPassword())) {
            // 账号不存在与密码错误返回同一提示，避免被用来探测账号是否存在
            throw new BusinessException(1001, "账号或密码错误");
        }
        if (!UserRole.isEnabled(user.getStatus())) {
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

    /**
     * 自助注册。新用户的角色固定为 0（普通用户）、状态固定为 1（可用）。
     * <p>
     * 角色与状态不从表单接收——否则任何人都能通过构造请求把自己注册成管理员。
     */
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
        user.setRole(UserRole.NORMAL);
        user.setStatus(UserRole.STATUS_ENABLED);
        userMapper.insert(user);
    }

    /**
     * 查询本人资料。返回前把密码置空，避免哈希值随响应体泄漏到浏览器。
     */
    public User getProfile(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 修改本人资料，只允许改真实姓名与手机号。
     * <p>
     * 这里用新建对象只带可改字段的做法，而不是把整个表单覆盖上去：
     * 表单里即使出现 role、status、username 也会被忽略，防止借资料修改接口提权。
     */
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

    /**
     * 修改本人密码，必须先校验原密码。
     * <p>
     * 本项目改密后不会让旧令牌失效（需给 user 表加 token_version 字段才能做到），
     * 旧令牌仍可用满有效期，这一点在报告里作为改进方向说明。
     */
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

    /**
     * 管理端：分页查询用户，支持按账号、姓名、角色与状态筛选。
     * 列表里每条记录同样把密码置空后再返回。
     */
    public PageResult<User> pageForAdmin(UserQueryDTO query) {
        PageHelper.startPage(query.safePageNum(), query.safePageSize());
        List<User> list = userMapper.selectByCondition(query);
        // 摘要不出库，列表同样置空后再返回
        list.forEach(item -> item.setPassword(null));
        PageInfo<User> pageInfo = new PageInfo<>(list);
        return PageResult.of(pageInfo.getTotal(), list);
    }

    /**
     * 管理员新建账号。
     * <p>
     * 与自助注册的区别：管理员可以直接指定角色和状态，初始密码由管理员设定
     * （不设固定默认值，避免全站新账号共用一个弱口令）。
     *
     * @return 新账号的主键
     */
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

    /**
     * 管理员编辑账号：可改姓名、手机号、角色与状态，账号与密码不在此处修改。
     * <p>
     * 两条防呆规则都指向同一个风险——把管理端入口关死：
     * <ol>
     *   <li>不能停用当前登录的账号，否则提交成功的那一刻自己就掉线了；</li>
     *   <li>不能把最后一个启用的管理员降级为普通用户，否则没有人能再进管理端。</li>
     * </ol>
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateByAdmin(Long id, UserSaveDTO dto) {
        User exist = requireUser(id);
        // 允许改自己的资料，但不能在编辑里顺手把自己停用，否则当场失去入口
        if (id.equals(UserContext.userId()) && !UserRole.isEnabled(dto.getStatus())) {
            throw new BusinessException(1001, "不能停用当前登录的账号");
        }
        // 降级是可以让系统失去管理入口的唯一路径：停用自己已被上一句拦住，
        // 而唯一的管理员只能是自己，所以「最后一名管理员」的判定实际只在降级时生效。
        boolean demote = UserRole.isAdmin(exist.getRole()) && !UserRole.isAdmin(dto.getRole());
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

    /**
     * 启用或停用账号。停用后该账号的旧令牌会立刻失效，
     * 因为拦截器每次请求都回查数据库状态，见 AuthInterceptor。
     *
     * @param status 0 停用，1 启用，其它值一律拒绝
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatusByAdmin(Long id, Integer status) {
        if (status == null
                || (status != UserRole.STATUS_DISABLED && status != UserRole.STATUS_ENABLED)) {
            throw new BusinessException(400, "账号状态只能为 0（禁用）或 1（可用）");
        }
        requireUser(id);
        if (status == UserRole.STATUS_DISABLED && id.equals(UserContext.userId())) {
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

    /**
     * 按主键取用户，查不到直接抛业务异常，作为写操作前的存在性断言。
     */
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
