package com.badminton.config;

import com.badminton.security.JwtAccessDeniedHandler;
import com.badminton.security.JwtAuthenticationEntryPoint;
import com.badminton.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置
 * 使用 Spring Security 做授权兜底，具体 JWT 校验由 JwtAuthenticationFilter + LoginInterceptor 共同完成
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（前后端分离 + JWT）
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用默认表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用 HTTP Basic 认证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 无状态 session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 注册 JWT 认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 异常处理：统一返回项目 Result 格式
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                // 授权规则：公开接口放行，其余 /api/** 需认证
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/login", "/api/user/login/password", "/api/user/register").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/venue/list", "/api/venue/detail/*", "/api/venue/slots",
                                "/api/coach/list", "/api/coach/detail/*", "/api/coach/schedule",
                                "/api/shop/categories", "/api/shop/products", "/api/shop/detail/*"
                        ).permitAll()
                        .requestMatchers("/doc.html", "/webjars/**", "/favicon.ico", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
