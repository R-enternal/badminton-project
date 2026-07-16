package com.badminton.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求参数
 */
@Data
public class LoginDTO {

    /**
     * 微信登录临时凭证 code
     */
    @NotBlank(message = "微信授权code不能为空")
    private String code;
}
