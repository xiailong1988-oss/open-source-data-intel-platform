package com.haidian.intel.platform.modules.system.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础测试接口。
 * 该接口用于验证工程路由、统一响应结构和接口文档是否已经正常可用。
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "基础测试")
public class TestController {

    @GetMapping("/ping")
    @Operation(summary = "基础 ping 接口")
    public ApiResponse<Map<String, Object>> ping() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("result", "pong");
        data.put("time", LocalDateTime.now());
        return ApiResponse.success("ping ok", data);
    }
}
