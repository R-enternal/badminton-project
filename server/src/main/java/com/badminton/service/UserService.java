package com.badminton.service;

import com.badminton.dto.LoginDTO;
import com.badminton.dto.PasswordLoginDTO;
import com.badminton.dto.RegisterDTO;
import com.badminton.vo.LoginVO;
import com.badminton.vo.UserInfoVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 账号密码登录
     *
     * @param loginDTO 登录参数
     * @return 登录结果（token + 用户信息）
     */
    LoginVO passwordLogin(PasswordLoginDTO loginDTO);

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 注册结果（token + 用户信息）
     */
    LoginVO register(RegisterDTO registerDTO);

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
