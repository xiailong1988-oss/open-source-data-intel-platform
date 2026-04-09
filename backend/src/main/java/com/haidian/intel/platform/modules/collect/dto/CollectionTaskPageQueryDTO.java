package com.haidian.intel.platform.modules.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Query parameters for task paging.
 */
@Getter
@Setter
@Schema(description = "Collection task page query")
public class CollectionTaskPageQueryDTO {

    @Min(value = 1, message = "pageNum must be greater than 0")
    @Schema(description = "Page number", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "pageSize must be greater than 0")
    @Max(value = 100, message = "pageSize must not be greater than 100")
    @Schema(description = "Page size", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Schema(description = "Keyword for task name or source name")
    private String keyword;

    @Schema(description = "Collection source ID")
    private Long sourceId;

    @Schema(description = "Task type code")
    private String taskType;

    @Min(value = 0, message = "enabled must be 0 or 1")
    @Max(value = 1, message = "enabled must be 0 or 1")
    @Schema(description = "Enable flag")
    private Integer enabled;

    @Schema(description = "Derived task status code")
    private String status;
}
