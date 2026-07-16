package com.badminton.common;

import lombok.Getter;

/**
 * 业务异常
 * 用于区分可预期的业务错误和不可预期的系统错误
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码，默认 500
     */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
