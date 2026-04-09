package com.haidian.intel.platform.modules.packagecenter.dto;

import com.haidian.intel.platform.common.constant.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Query parameters for data package paging.
 */
@Getter
@Setter
@Schema(description = "Data package page query")
public class DataPackagePageQueryDTO {

    @Min(value = 1, message = "pageNum must be greater than 0")
    @Schema(description = "Page number", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize must be greater than 0")
    @Max(value = 100, message = "pageSize must not be greater than 100")
    @Schema(description = "Page size", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "Keyword for package name or region scope")
    private String keyword;

    @Schema(description = "Data package type code")
    private String packageType;

    @Pattern(regexp = ValidationPatterns.OPTIONAL_YEAR_MONTH, message = "statMonth must match yyyy-MM")
    @Schema(description = "Statistic month, such as 2026-04")
    private String statMonth;

    @Schema(description = "Package status code")
    private String status;
}
