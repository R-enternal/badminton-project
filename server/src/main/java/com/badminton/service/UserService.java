package com.badminton.service;

import com.badminton.dto.LoginDTO;
import com.badminton.vo.LoginVO;
import com.badminton.vo.UserInfoVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 微信登录
     *
     * @param loginDTO 登录参数
     * @return 登录结果（token + 用户信息）
     */
    LoginVO wxLogin(LoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoVO getUserInfo(Long userId);
}
