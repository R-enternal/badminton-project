package com.badminton.common;

/**
 * 校验常量
 * 统一前后端共用的校验规则，避免规则散落在各 DTO 中
 */
public final class ValidationConstants {

    private ValidationConstants() {
    }

    /**
     * 中国大陆手机号正则
     */
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    /**
     * 手机号格式错误提示
     */
    public static final String PHONE_MESSAGE = "手机号格式不正确";

    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN = 6;

    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX = 20;

    /**
     * 密码长度错误提示
     */
    public static final String PASSWORD_SIZE_MESSAGE = "密码长度必须在6-20位之间";
}
