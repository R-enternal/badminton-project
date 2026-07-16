package com.badminton.config;

import com.badminton.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 * 注册登录拦截器并配置放行路径
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 拦截所有 /api/** 请求
                .addPathPatterns("/api/**")
                // 放行登录接口
                .excludePathPatterns("/api/user/login")
                // 放行场地/教练/商品查询类接口（未登录也能浏览）
                .excludePathPatterns("/api/venue/list", "/api/venue/detail/*", "/api/venue/slots")
                .excludePathPatterns("/api/coach/list", "/api/coach/detail/*", "/api/coach/schedule")
                .excludePathPatterns("/api/shop/categories", "/api/shop/products", "/api/shop/detail/*")
                // 放行 Swagger / Knife4j 文档
                .excludePathPatterns("/doc.html", "/webjars/**", "/favicon.ico", "/v3/api-docs/**", "/swagger-ui/**")
                // 放行健康检查
                .excludePathPatterns("/");
    }
}
