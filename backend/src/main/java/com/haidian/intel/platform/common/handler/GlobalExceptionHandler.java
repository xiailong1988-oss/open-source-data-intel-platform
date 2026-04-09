package com.haidian.intel.platform.common.handler;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器。
 * 统一收口参数校验、业务异常和未知异常，保证接口层输出结构稳定。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        return ApiResponse.failure(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResponse.failure(ResultCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResponse.failure(ResultCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        return ApiResponse.failure(ResultCode.VALIDATION_ERROR, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        String message = "参数 " + exception.getName() + " 类型不正确";
        return ApiResponse.failure(ResultCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return ApiResponse.failure(ResultCode.BAD_REQUEST, "请求体格式不正确");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("Unhandled exception captured by global handler.", exception);
        return ApiResponse.failure(ResultCode.INTERNAL_ERROR);
    }
}
