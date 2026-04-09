package com.haidian.intel.platform.modules.source.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.auth.RequireRole;
import com.haidian.intel.platform.modules.source.dto.CollectionSourceCreateDTO;
import com.haidian.intel.platform.modules.source.dto.CollectionSourcePageQueryDTO;
import com.haidian.intel.platform.modules.source.dto.CollectionSourceUpdateDTO;
import com.haidian.intel.platform.modules.source.service.CollectionSourceService;
import com.haidian.intel.platform.modules.source.vo.CollectionSourceDetailVO;
import com.haidian.intel.platform.modules.source.vo.CollectionSourcePageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
 * Controller for collection source management APIs.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sources")
@Tag(name = "数据源管理")
@RequireRole("ADMIN")
public class CollectionSourceController {

    private final CollectionSourceService collectionSourceService;

    @GetMapping("/page")
    @Operation(summary = "分页查询数据源")
    public ApiResponse<PageResponse<CollectionSourcePageVO>> pageSources(
            @Valid @ParameterObject CollectionSourcePageQueryDTO queryDTO
    ) {
        return ApiResponse.success(collectionSourceService.pageSources(queryDTO));
    }

    @GetMapping("/{sourceId}")
    @Operation(summary = "查询数据源详情")
    public ApiResponse<CollectionSourceDetailVO> getSourceDetail(
            @PathVariable("sourceId") @Positive(message = "sourceId must be greater than 0") Long sourceId
    ) {
        return ApiResponse.success(collectionSourceService.getSourceDetail(sourceId));
    }

    @PostMapping
    @Operation(summary = "新增数据源")
    public ApiResponse<CollectionSourceDetailVO> createSource(
            @Valid @RequestBody CollectionSourceCreateDTO createDTO
    ) {
        return ApiResponse.success(collectionSourceService.createSource(createDTO));
    }

    @PutMapping("/{sourceId}")
    @Operation(summary = "编辑数据源")
    public ApiResponse<CollectionSourceDetailVO> updateSource(
            @PathVariable("sourceId") @Positive(message = "sourceId must be greater than 0") Long sourceId,
            @Valid @RequestBody CollectionSourceUpdateDTO updateDTO
    ) {
        return ApiResponse.success(collectionSourceService.updateSource(sourceId, updateDTO));
    }

    @DeleteMapping("/{sourceId}")
    @Operation(summary = "删除数据源")
    public ApiResponse<Void> deleteSource(
            @PathVariable("sourceId") @Positive(message = "sourceId must be greater than 0") Long sourceId
    ) {
        collectionSourceService.deleteSource(sourceId);
        return ApiResponse.success(null);
    }
}
