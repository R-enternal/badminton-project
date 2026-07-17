package com.badminton.config;

import com.badminton.interceptor.LoginInterceptor;
import com.badminton.interceptor.RoleInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web 配置
 * 注册登录拦截器、角色拦截器并配置放行路径
 * 注：Spring Security 已做第一层授权兜底，拦截器做第二层业务校验
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final RoleInterceptor roleInterceptor;

    /**
     * 公开接口：无需登录、无需角色校验
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/user/login", "/api/user/login/password", "/api/user/register",
            "/api/venue/list", "/api/venue/detail/*", "/api/venue/slots",
            "/api/coach/list", "/api/coach/detail/*", "/api/coach/schedule",
            "/api/shop/categories", "/api/shop/products", "/api/shop/detail/*",
            "/doc.html", "/webjars/**", "/favicon.ico", "/v3/api-docs/**", "/swagger-ui/**",
            "/"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(PUBLIC_PATHS);

        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(PUBLIC_PATHS);
    }
}
