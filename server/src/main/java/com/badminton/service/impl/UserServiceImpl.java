package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.LoginDTO;
import com.badminton.dto.PasswordLoginDTO;
import com.badminton.dto.RegisterDTO;
import com.badminton.entity.SysUser;
import com.badminton.mapper.SysUserMapper;
import com.badminton.service.UserService;
import com.badminton.util.JwtUtil;
import com.badminton.vo.LoginVO;
import com.badminton.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO passwordLogin(PasswordLoginDTO loginDTO) {
        // 1. 根据手机号查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, loginDTO.getPhone())
        );

        if (user == null) {
            throw new BusinessException("手机号或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }

        // 3. 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // 4. 组装返回
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(convertToVO(user));
        return loginVO;
    }

    @Override
    public LoginVO register(RegisterDTO registerDTO) {
        // 1. 校验手机号是否已注册
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, registerDTO.getPhone())
        );
        if (count != null && count > 0) {
            throw new BusinessException("该手机号已注册");
        }

        // 2. 创建用户
        SysUser user = new SysUser();
        user.setPhone(registerDTO.getPhone());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : "球友" + registerDTO.getPhone().substring(7));
        user.setRole("USER");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(user);

        // 3. 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // 4. 组装返回
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(convertToVO(user));
        return loginVO;
    }

    @Override
    public LoginVO wxLogin(LoginDTO loginDTO) {
        // 开发阶段：根据 code 生成一个稳定的 openid，便于测试
        String openid = "test_openid_" + loginDTO.getCode().hashCode();

        // 1. 根据 openid 查询或创建用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getOpenid, openid)
        );

        if (user == null) {
            user = new SysUser();
            user.setOpenid(openid);
            user.setNickname("微信用户" + openid.substring(openid.length() - 6));
            user.setPhone(null);
            user.setPassword(passwordEncoder.encode(""));
            user.setRole("USER");
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            sysUserMapper.insert(user);
        }

        // 2. 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        // 3. 组装返回
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(convertToVO(user));
        return loginVO;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    private UserInfoVO convertToVO(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        return vo;
    }
}
