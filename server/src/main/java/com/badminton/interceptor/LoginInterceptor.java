package com.badminton.interceptor;

import com.badminton.util.JwtUtil;
import com.badminton.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * 校验请求头中的 JWT token，并将 userId 存入 ThreadLocal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            throw new RuntimeException("请先登录");
        }

        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            throw new RuntimeException("登录已过期，请重新登录");
        }

        Long userId = jwtUtil.parseUserId(token);
        String role = jwtUtil.parseRole(token);
        UserContext.setUserId(userId);
        UserContext.setRole(role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，防止内存泄漏
        UserContext.remove();
    }
}
