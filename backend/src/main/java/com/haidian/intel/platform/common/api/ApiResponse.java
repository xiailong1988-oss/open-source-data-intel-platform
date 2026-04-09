package com.haidian.intel.platform.common.api;

import com.haidian.intel.platform.common.enums.ResultCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 统一接口响应体。
 * 当前阶段所有 Controller 都必须通过该对象返回结果，避免前后端联调时出现结构漂移。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一接口响应")
public class ApiResponse<T> {

    @Schema(description = "业务状态码", example = "0")
    private Integer code;

    @Schema(description = "响应说明", example = "success")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "服务端响应时间", example = "2026-04-07T16:00:00")
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getMessage(),
                data,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                ResultCode.SUCCESS.getCode(),
                message,
                data,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> failure(ResultCode resultCode) {
        return failure(resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> ApiResponse<T> failure(ResultCode resultCode, String message) {
        return failure(resultCode.getCode(), message);
    }

    public static <T> ApiResponse<T> failure(Integer code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }
}
