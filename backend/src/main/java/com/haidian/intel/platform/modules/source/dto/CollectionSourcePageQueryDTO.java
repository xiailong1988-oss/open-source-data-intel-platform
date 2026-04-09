package com.haidian.intel.platform.modules.source.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Query parameters for source paging.
 */
@Getter
@Setter
@Schema(description = "Collection source page query")
public class CollectionSourcePageQueryDTO {

    @Min(value = 1, message = "pageNum must be greater than 0")
    @Schema(description = "Page number", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize must be greater than 0")
    @Max(value = 100, message = "pageSize must not be greater than 100")
    @Schema(description = "Page size", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "Keyword for source name, owner, or endpoint")
    private String keyword;

    @Schema(description = "Source category code")
    private String sourceCategory;

    @Schema(description = "Access type code")
    private String accessType;

    @Min(value = 0, message = "enabled must be 0 or 1")
    @Max(value = 1, message = "enabled must be 0 or 1")
    @Schema(description = "Enable flag")
    private Integer enabled;

    @Schema(description = "Source status code")
    private String status;
}
