package com.haidian.intel.platform.modules.system.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.modules.system.service.SystemHealthService;
import com.haidian.intel.platform.modules.system.vo.HealthStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统基础健康检查接口。
 * 该接口用于前端和运维快速确认服务是否已经完成应用层启动。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "系统基础")
public class HealthController {

    private final SystemHealthService systemHealthService;

    @GetMapping("/health")
    @Operation(summary = "获取应用健康状态")
    public ApiResponse<HealthStatusVO> getHealthStatus() {
        return ApiResponse.success(systemHealthService.getHealthStatus());
    }
}
