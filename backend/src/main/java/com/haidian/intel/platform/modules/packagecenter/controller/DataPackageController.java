package com.haidian.intel.platform.modules.packagecenter.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.auth.RequireRole;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackageCreateDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackagePageQueryDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackagePublishDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackageUpdateDTO;
import com.haidian.intel.platform.modules.packagecenter.service.DataPackageService;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackageDetailVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackageFileVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackagePageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for data package management APIs.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/packages")
@Tag(name = "Data Package Management")
@RequireRole("ADMIN")
public class DataPackageController {

    private final DataPackageService dataPackageService;

    @GetMapping("/page")
    @Operation(summary = "Page data packages")
    public ApiResponse<PageResponse<DataPackagePageVO>> pagePackages(
            @Valid @ParameterObject DataPackagePageQueryDTO queryDTO
    ) {
        return ApiResponse.success(dataPackageService.pagePackages(queryDTO));
    }

    @GetMapping("/{packageId}")
    @Operation(summary = "Get data package detail")
    public ApiResponse<DataPackageDetailVO> getPackageDetail(
            @PathVariable("packageId") @Positive(message = "packageId must be greater than 0") Long packageId
    ) {
        return ApiResponse.success(dataPackageService.getPackageDetail(packageId));
    }

    @PostMapping
    @Operation(summary = "Create data package")
    public ApiResponse<DataPackageDetailVO> createPackage(
            @Valid @RequestBody DataPackageCreateDTO createDTO
    ) {
        return ApiResponse.success(dataPackageService.createPackage(createDTO));
    }

    @PutMapping("/{packageId}")
    @Operation(summary = "Update data package")
    public ApiResponse<DataPackageDetailVO> updatePackage(
            @PathVariable("packageId") @Positive(message = "packageId must be greater than 0") Long packageId,
            @Valid @RequestBody DataPackageUpdateDTO updateDTO
    ) {
        return ApiResponse.success(dataPackageService.updatePackage(packageId, updateDTO));
    }

    @PostMapping("/{packageId}/publish")
    @Operation(summary = "Publish data package")
    public ApiResponse<DataPackageDetailVO> publishPackage(
            @PathVariable("packageId") @Positive(message = "packageId must be greater than 0") Long packageId,
            @Valid @RequestBody(required = false) DataPackagePublishDTO publishDTO
    ) {
        return ApiResponse.success(dataPackageService.publishPackage(packageId, publishDTO));
    }

    @GetMapping("/{packageId}/files")
    @Operation(summary = "List data package files")
    public ApiResponse<List<DataPackageFileVO>> listPackageFiles(
            @PathVariable("packageId") @Positive(message = "packageId must be greater than 0") Long packageId
    ) {
        return ApiResponse.success(dataPackageService.listPackageFiles(packageId));
    }
}
