package com.badminton.vo;

import lombok.Data;

/**
 * 登录响应 VO
 */
@Data
public class LoginVO {

    /**
     * JWT token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserInfoVO userInfo;
}
