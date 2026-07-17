package com.badminton.dto;

import com.badminton.common.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求参数
 */
@Data
public class RegisterDTO {

    /**
     * 手机号，作为登录账号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = ValidationConstants.PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
    private String phone;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = ValidationConstants.PASSWORD_MIN, max = ValidationConstants.PASSWORD_MAX, message = ValidationConstants.PASSWORD_SIZE_MESSAGE)
    private String password;

    /**
     * 昵称
     */
    @Size(max = 32, message = "昵称长度不能超过32位")
    private String nickname;
}
