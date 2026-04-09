package com.haidian.intel.platform.modules.region.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.modules.region.dto.MapPointQueryDTO;
import com.haidian.intel.platform.modules.region.service.MapPointService;
import com.haidian.intel.platform.modules.region.vo.MapPointVO;
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
 * 地图点位接口。
 * 当前只负责首页点位数据查询，不扩展聚合统计或专题能力。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
@Tag(name = "地图管理")
public class MapPointController {

    private final MapPointService mapPointService;

    @GetMapping("/points")
    @Operation(summary = "获取首页地图点位")
    public ApiResponse<List<MapPointVO>> listMapPoints(@Valid @ParameterObject MapPointQueryDTO queryDTO) {
        return ApiResponse.success(mapPointService.listMapPoints(queryDTO));
    }
}
