package com.haidian.intel.platform.modules.packagecenter.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.auth.RequireRole;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordCreateDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordPageQueryDTO;
import com.haidian.intel.platform.modules.packagecenter.service.DeliveryRecordService;
import com.haidian.intel.platform.modules.packagecenter.vo.DeliveryRecordDetailVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DeliveryRecordPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for delivery record management APIs.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery-records")
@Tag(name = "Delivery Record Management")
@RequireRole("ADMIN")
public class DeliveryRecordController {

    private final DeliveryRecordService deliveryRecordService;

    @GetMapping("/page")
    @Operation(summary = "Page delivery records")
    public ApiResponse<PageResponse<DeliveryRecordPageVO>> pageRecords(
            @Valid @ParameterObject DeliveryRecordPageQueryDTO queryDTO
    ) {
        return ApiResponse.success(deliveryRecordService.pageRecords(queryDTO));
    }

    @PostMapping
    @Operation(summary = "Create delivery record")
    public ApiResponse<DeliveryRecordDetailVO> createRecord(
            @Valid @RequestBody DeliveryRecordCreateDTO createDTO
    ) {
        return ApiResponse.success(deliveryRecordService.createRecord(createDTO));
    }

    @GetMapping("/{recordId}")
    @Operation(summary = "Get delivery record detail")
    public ApiResponse<DeliveryRecordDetailVO> getRecordDetail(
            @PathVariable("recordId") @Positive(message = "recordId must be greater than 0") Long recordId
    ) {
        return ApiResponse.success(deliveryRecordService.getRecordDetail(recordId));
    }
}
