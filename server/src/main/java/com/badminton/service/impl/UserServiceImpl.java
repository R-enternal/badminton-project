package com.badminton.service.impl;

import com.badminton.common.BusinessException;
import com.badminton.dto.LoginDTO;
import com.badminton.entity.SysUser;
import com.badminton.mapper.SysUserMapper;
import com.badminton.service.UserService;
import com.badminton.util.JwtUtil;
import com.badminton.vo.LoginVO;
import com.badminton.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 * 当前为开发阶段，使用模拟微信登录（根据 code 生成 openid）
 * 上线前替换为真实微信 jscode2session 调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO wxLogin(LoginDTO loginDTO) {
        // ==================== 开发阶段：模拟登录 ====================
        // 根据 code 生成一个稳定的 openid，便于测试
        // 上线前替换为下方真实微信登录逻辑
        String openid = "test_openid_" + loginDTO.getCode().hashCode();

        // ==================== 真实微信登录（上线前启用）====================
        // String url = String.format(
        //     "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
        //     appId, appSecret, loginDTO.getCode());
        // JSONObject result = JSONUtil.parseObj(HttpUtil.get(url));
        // String openid = result.getStr("openid");
        // if (StrUtil.isBlank(openid)) {
        //     throw new BusinessException("微信登录失败");
        // }

        // 1. 根据 openid 查询或创建用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getOpenid, openid)
        );

        if (user == null) {
            user = new SysUser();
            user.setOpenid(openid);
            user.setNickname("微信用户" + openid.substring(openid.length() - 6));
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            sysUserMapper.insert(user);
        }

        // 2. 生成 JWT token
        String token = jwtUtil.generateToken(user.getId());

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
        return vo;
    }
}
