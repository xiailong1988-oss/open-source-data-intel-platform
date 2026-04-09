package com.haidian.intel.platform.modules.dashboard.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardFocusTargetQueryDTO;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardHotspotQueryDTO;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardStatsQueryDTO;
import com.haidian.intel.platform.modules.dashboard.service.DashboardService;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardFocusTargetVO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardHotspotVO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardOverviewVO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardRegionSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页概览接口。
 * 当前仅承接顶部概览统计和地区态势摘要，不扩展热点专题等后续模块。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
@Tag(name = "首页概览")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "获取首页概览统计")
    public ApiResponse<DashboardOverviewVO> getOverview(@Valid @ParameterObject DashboardStatsQueryDTO queryDTO) {
        return ApiResponse.success(dashboardService.getOverview(queryDTO));
    }

    @GetMapping("/region-summary")
    @Operation(summary = "获取地区态势摘要")
    public ApiResponse<DashboardRegionSummaryVO> getRegionSummary(
            @Valid @ParameterObject DashboardStatsQueryDTO queryDTO
    ) {
        return ApiResponse.success(dashboardService.getRegionSummary(queryDTO));
    }

    @GetMapping("/hotspots")
    @Operation(summary = "获取首页热点专题")
    public ApiResponse<List<DashboardHotspotVO>> listHotspots(
            @Valid @ParameterObject DashboardHotspotQueryDTO queryDTO
    ) {
        return ApiResponse.success(dashboardService.listHotspots(queryDTO));
    }

    @GetMapping("/focus-targets")
    @Operation(summary = "获取首页重点关注对象")
    public ApiResponse<List<DashboardFocusTargetVO>> listFocusTargets(
            @Valid @ParameterObject DashboardFocusTargetQueryDTO queryDTO
    ) {
        return ApiResponse.success(dashboardService.listFocusTargets(queryDTO));
    }
}
