package com.haidian.intel.platform.common.enums;

import lombok.Getter;

/**
 * 统一业务状态码定义。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "success"),
    BAD_REQUEST(4000, "bad request"),
    VALIDATION_ERROR(4001, "validation failed"),
    UNAUTHORIZED(4003, "unauthorized"),
    NOT_FOUND(4004, "resource not found"),
    FORBIDDEN(4005, "forbidden"),
    INTERNAL_ERROR(5000, "internal server error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
