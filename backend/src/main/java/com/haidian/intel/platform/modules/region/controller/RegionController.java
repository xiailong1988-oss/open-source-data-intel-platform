package com.haidian.intel.platform.modules.region.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.modules.region.dto.RegionTreeQueryDTO;
import com.haidian.intel.platform.modules.region.service.RegionService;
import com.haidian.intel.platform.modules.region.vo.RegionDetailVO;
import com.haidian.intel.platform.modules.region.vo.RegionGeoVO;
import com.haidian.intel.platform.modules.region.vo.RegionTreeNodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 地区基础接口。
 * 当前用于支撑首页地区切换、地区详情读取和边界 GeoJSON 加载。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/regions")
@Tag(name = "地区管理")
public class RegionController {

    private final RegionService regionService;

    @GetMapping("/tree")
    @Operation(summary = "获取地区树")
    public ApiResponse<List<RegionTreeNodeVO>> getRegionTree(@ParameterObject RegionTreeQueryDTO queryDTO) {
        return ApiResponse.success(regionService.getRegionTree(queryDTO));
    }

    @GetMapping("/{regionId}")
    @Operation(summary = "获取地区详情")
    public ApiResponse<RegionDetailVO> getRegionDetail(
            @PathVariable("regionId") @Positive(message = "地区ID必须大于0") Long regionId
    ) {
        return ApiResponse.success(regionService.getRegionDetail(regionId));
    }

    @GetMapping("/{regionId}/geo")
    @Operation(summary = "获取地区边界 GeoJSON")
    public ApiResponse<RegionGeoVO> getRegionGeo(
            @PathVariable("regionId") @Positive(message = "地区ID必须大于0") Long regionId
    ) {
        return ApiResponse.success(regionService.getRegionGeo(regionId));
    }
}
