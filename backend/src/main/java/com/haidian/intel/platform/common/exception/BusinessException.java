package com.haidian.intel.platform.common.exception;

import com.haidian.intel.platform.common.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常基类。
 * 后续模块在明确业务分支时统一抛出该异常，避免直接暴露底层异常信息给前端。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
