package com.example.meetingroom.interceptor;

import com.example.meetingroom.common.BusinessException;
import com.example.meetingroom.common.LoginUser;
import com.example.meetingroom.common.UserContext;
import com.example.meetingroom.common.UserRole;
import com.example.meetingroom.entity.User;
import com.example.meetingroom.mapper.UserMapper;
import com.example.meetingroom.util.TokenUtil;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器：解析请求头 Authorization 中的令牌，写入用户上下文。
 * 同时完成管理端 /api/admin/** 的角色校验，避免越权。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || !(handler instanceof HandlerMethod)) {
            return true;
        }
        // 兜底：Tomcat 复用工作线程，任何异常路径留下的身份都不能带进本次请求
        UserContext.clear();

        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : header;

        LoginUser parsed = tokenUtil.parse(token);
        if (parsed == null) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        // parsed 里的 username/role 只是签发时的快照，不能用于授权判断，只取 userId。

        // 令牌只证明签名有效、未过期，并不代表账号此刻仍可用。
        // 管理员停用或调整角色后，对方手上的旧令牌在没有这一层回查时仍能用满整个有效期，
        // 等于「停用」只对下一次登录生效。这里以数据库为准刷新状态与角色。
        User current = userMapper.findById(parsed.getUserId());
        if (current == null) {
            throw new BusinessException(401, "账号不存在，请重新登录");
        }
        if (!UserRole.isEnabled(current.getStatus())) {
            throw new BusinessException(401, "账号已被停用，请联系管理员");
        }

        // 先做 403 判断、再写 ThreadLocal：preHandle 抛异常时 Spring MVC 不会回调本拦截器的
        // afterCompletion（interceptorIndex 未推进），若先 set 后抛，身份会残留在复用的线程上。
        // 约定：UserContext.set 之后到 return true 之间不得再抛异常。
        if (request.getRequestURI().startsWith("/api/admin/") && !UserRole.isAdmin(current.getRole())) {
            throw new BusinessException(403, "该操作仅管理员可执行");
        }
        UserContext.set(new LoginUser(current.getId(), current.getUsername(),
                current.getRole(), current.getRealName()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
