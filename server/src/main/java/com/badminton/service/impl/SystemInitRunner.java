package com.badminton.service.impl;

import com.badminton.entity.SysUser;
import com.badminton.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 系统初始化
 * 启动时检查是否存在管理员，不存在则创建默认管理员
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemInitRunner implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.phone:13800138000}")
    private String defaultAdminPhone;

    @Value("${admin.default.password:}")
    private String defaultAdminPassword;

    @Override
    public void run(String... args) {
        Long adminCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "ADMIN")
        );
        if (adminCount != null && adminCount > 0) {
            return;
        }

        if (!StringUtils.hasText(defaultAdminPassword)) {
            log.warn("========================================");
            log.warn("系统中没有管理员，但未配置默认管理员密码");
            log.warn("请设置环境变量 DEFAULT_ADMIN_PASSWORD 后重启");
            log.warn("========================================");
            return;
        }

        SysUser admin = new SysUser();
        admin.setPhone(defaultAdminPhone);
        admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
        admin.setNickname("超级管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(admin);

        log.info("========================================");
        log.info("已创建默认管理员账号");
        log.info("手机号：{}", defaultAdminPhone);
        log.info("========================================");
    }
}
