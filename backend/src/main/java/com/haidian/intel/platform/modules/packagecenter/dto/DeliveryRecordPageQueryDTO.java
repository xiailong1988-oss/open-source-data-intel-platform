package com.haidian.intel.platform.modules.packagecenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Query parameters for delivery record paging.
 */
@Getter
@Setter
@Schema(description = "Delivery record page query")
public class DeliveryRecordPageQueryDTO {

    @Min(value = 1, message = "pageNum must be greater than 0")
    @Schema(description = "Page number", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize must be greater than 0")
    @Max(value = 100, message = "pageSize must not be greater than 100")
    @Schema(description = "Page size", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "Keyword for related name or receiver")
    private String keyword;

    @Schema(description = "Delivery type code")
    private String deliveryType;

    @Positive(message = "relatedId must be greater than 0")
    @Schema(description = "Related business ID")
    private Long relatedId;

    @Schema(description = "Delivery method code")
    private String deliveryMethod;
}
