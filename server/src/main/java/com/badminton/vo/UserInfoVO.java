package com.badminton.vo;

import lombok.Data;

/**
 * 用户信息 VO
 */
@Data
public class UserInfoVO {

    private Long id;

    private String nickname;

    private String avatar;

    private String phone;
}
