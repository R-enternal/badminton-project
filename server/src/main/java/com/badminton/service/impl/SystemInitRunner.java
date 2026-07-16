package com.badminton.service.impl;

import com.badminton.entity.SysUser;
import com.badminton.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    @Override
    public void run(String... args) {
        Long adminCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "ADMIN")
        );
        if (adminCount != null && adminCount > 0) {
            return;
        }

        SysUser admin = new SysUser();
        admin.setPhone("13800138000");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("超级管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(admin);

        log.info("========================================");
        log.info("已创建默认管理员账号");
        log.info("手机号：13800138000");
        log.info("密码：admin123");
        log.info("========================================");
    }
}
