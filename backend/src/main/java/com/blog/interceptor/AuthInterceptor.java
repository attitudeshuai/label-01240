package com.blog.interceptor;

import com.blog.exception.BusinessException;
import com.blog.service.TokenBlacklistService;
import com.blog.util.JwtUtil;
import com.blog.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * 认证拦截器
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // 允许匿名访问的GET接口
    private static final List<String> PUBLIC_GET_PATTERNS = Arrays.asList(
            "/api/blogs",
            "/api/blogs/",
            "/api/comments/blog/",
            "/api/users/"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String token = request.getHeader("Authorization");

        // 检查是否是公开的GET接口
        boolean isPublicGet = "GET".equalsIgnoreCase(method) && isPublicPath(uri);

        // 如果没有token
        if (!StringUtils.hasText(token)) {
            if (isPublicGet) {
                // 公开GET接口允许匿名访问
                return true;
            }
            throw new BusinessException(401, "未登录");
        }

        // 去掉Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 检查token是否在黑名单中（已登出）
        if (tokenBlacklistService.isBlacklisted(token)) {
            if (isPublicGet) {
                return true;
            }
            throw new BusinessException(401, "登录已失效，请重新登录");
        }

        if (!jwtUtil.validateToken(token)) {
            if (isPublicGet) {
                // 公开GET接口即使token无效也允许访问
                return true;
            }
            throw new BusinessException(401, "登录已过期，请重新登录");
        }

        // 设置用户上下文
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        Integer role = jwtUtil.getRole(token);

        UserContext.setUserId(userId);
        UserContext.setUsername(username);
        UserContext.setRole(role);

        log.debug("用户认证成功: userId={}, username={}, role={}", userId, username, role);
        return true;
    }

    /**
     * 检查是否是公开路径
     */
    private boolean isPublicPath(String uri) {
        for (String pattern : PUBLIC_GET_PATTERNS) {
            if (uri.equals(pattern) || uri.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
