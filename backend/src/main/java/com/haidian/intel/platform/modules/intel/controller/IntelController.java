package com.haidian.intel.platform.modules.intel.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.auth.RequireRole;
import com.haidian.intel.platform.modules.intel.dto.IntelCreateDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelPageQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelStreamQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelTickerQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelUpdateDTO;
import com.haidian.intel.platform.modules.intel.service.IntelService;
import com.haidian.intel.platform.modules.intel.vo.IntelDetailVO;
import com.haidian.intel.platform.modules.intel.vo.IntelPageVO;
import com.haidian.intel.platform.modules.intel.vo.IntelStreamVO;
import com.haidian.intel.platform.modules.intel.vo.IntelTickerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 情报核心接口。
 * 当前同时提供后台管理所需 CRUD 能力，以及首页联调所需快讯条和主情报流接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intel")
@Tag(name = "情报管理")
public class IntelController {

    private final IntelService intelService;

    @GetMapping("/page")
    @Operation(summary = "分页查询情报")
    @RequireRole("ADMIN")
    public ApiResponse<PageResponse<IntelPageVO>> pageIntel(@Valid @ParameterObject IntelPageQueryDTO queryDTO) {
        return ApiResponse.success(intelService.pageIntel(queryDTO));
    }

    @GetMapping("/ticker")
    @Operation(summary = "获取首页快讯条")
    public ApiResponse<List<IntelTickerVO>> listIntelTicker(@Valid @ParameterObject IntelTickerQueryDTO queryDTO) {
        return ApiResponse.success(intelService.listIntelTicker(queryDTO));
    }

    @GetMapping("/stream")
    @Operation(summary = "获取首页主情报流")
    public ApiResponse<PageResponse<IntelStreamVO>> pageIntelStream(
            @Valid @ParameterObject IntelStreamQueryDTO queryDTO
    ) {
        return ApiResponse.success(intelService.pageIntelStream(queryDTO));
    }

    @GetMapping("/{intelId}")
    @Operation(summary = "获取情报详情")
    @RequireRole("ADMIN")
    public ApiResponse<IntelDetailVO> getIntelDetail(
            @PathVariable("intelId") @Positive(message = "情报ID必须大于0") Long intelId
    ) {
        return ApiResponse.success(intelService.getIntelDetail(intelId));
    }

    @PostMapping
    @Operation(summary = "新增情报")
    @RequireRole("ADMIN")
    public ApiResponse<IntelDetailVO> createIntel(@Valid @RequestBody IntelCreateDTO createDTO) {
        return ApiResponse.success(intelService.createIntel(createDTO));
    }

    @PutMapping("/{intelId}")
    @Operation(summary = "修改情报")
    @RequireRole("ADMIN")
    public ApiResponse<IntelDetailVO> updateIntel(
            @PathVariable("intelId") @Positive(message = "情报ID必须大于0") Long intelId,
            @Valid @RequestBody IntelUpdateDTO updateDTO
    ) {
        return ApiResponse.success(intelService.updateIntel(intelId, updateDTO));
    }

    @DeleteMapping("/{intelId}")
    @Operation(summary = "删除情报")
    @RequireRole("ADMIN")
    public ApiResponse<Void> deleteIntel(
            @PathVariable("intelId") @Positive(message = "情报ID必须大于0") Long intelId
    ) {
        intelService.deleteIntel(intelId);
        return ApiResponse.success(null);
    }
}
