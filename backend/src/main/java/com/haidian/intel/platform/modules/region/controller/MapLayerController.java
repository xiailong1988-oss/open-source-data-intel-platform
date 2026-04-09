package com.haidian.intel.platform.modules.region.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.modules.region.dto.MapLayerQueryDTO;
import com.haidian.intel.platform.modules.region.service.MapLayerService;
import com.haidian.intel.platform.modules.region.vo.MapLayerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 地图图层基础接口。
 * 当前用于前端读取图层列表和样式配置，不扩展点位或聚合能力。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
@Tag(name = "地图管理")
public class MapLayerController {

    private final MapLayerService mapLayerService;

    @GetMapping("/layers")
    @Operation(summary = "获取地图图层配置")
    public ApiResponse<List<MapLayerVO>> getMapLayers(@Valid @ParameterObject MapLayerQueryDTO queryDTO) {
        return ApiResponse.success(mapLayerService.getMapLayers(queryDTO));
    }
}
