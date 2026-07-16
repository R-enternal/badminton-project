package com.badminton.interceptor;

import com.badminton.annotation.RequireRole;
import com.badminton.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 角色权限拦截器
 * 校验 Controller 方法上的 @RequireRole 注解
 */
@Slf4j
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            return true;
        }

        String currentRole = UserContext.getRole();
        if (currentRole == null) {
            response.setStatus(403);
            throw new RuntimeException("无权访问");
        }

        boolean allowed = Arrays.asList(requireRole.value()).contains(currentRole);
        if (!allowed) {
            response.setStatus(403);
            throw new RuntimeException("当前角色无权访问该接口");
        }

        return true;
    }
}
