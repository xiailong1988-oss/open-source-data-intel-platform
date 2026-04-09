package com.haidian.intel.platform.modules.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Base DTO for task create and update.
 */
@Getter
@Setter
public class CollectionTaskSaveDTO {

    @NotBlank(message = "taskName must not be blank")
    @Size(max = 255, message = "taskName length must not exceed 255")
    @Schema(description = "Task name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskName;

    @NotNull(message = "sourceId must not be null")
    @Positive(message = "sourceId must be greater than 0")
    @Schema(description = "Collection source ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sourceId;

    @NotBlank(message = "taskType must not be blank")
    @Size(max = 64, message = "taskType length must not exceed 64")
    @Schema(description = "Task type code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskType;

    @Size(max = 128, message = "cronExpr length must not exceed 128")
    @Schema(description = "Cron expression or schedule rule")
    private String cronExpr;

    @NotBlank(message = "frequencyDesc must not be blank")
    @Size(max = 64, message = "frequencyDesc length must not exceed 64")
    @Schema(description = "Human readable frequency", requiredMode = Schema.RequiredMode.REQUIRED)
    private String frequencyDesc;

    @Min(value = 0, message = "enabled must be 0 or 1")
    @Max(value = 1, message = "enabled must be 0 or 1")
    @Schema(description = "Enable flag", defaultValue = "1")
    private Integer enabled = 1;

    @Size(max = 32, message = "runStatus length must not exceed 32")
    @Schema(description = "Run status code", defaultValue = "running")
    private String runStatus = "running";

    @Size(max = 32, message = "latestResult length must not exceed 32")
    @Schema(description = "Latest run result code", defaultValue = "success")
    private String latestResult = "success";

    @Schema(description = "Latest run time")
    private LocalDateTime latestRunTime;

    @PositiveOrZero(message = "recordCount must not be negative")
    @Schema(description = "Collected record count", defaultValue = "0")
    private Long recordCount = 0L;

    @DecimalMin(value = "0.00", message = "successRate must be between 0 and 100")
    @DecimalMax(value = "100.00", message = "successRate must be between 0 and 100")
    @Schema(description = "Success rate", defaultValue = "0.00")
    private BigDecimal successRate = BigDecimal.ZERO;

    @Size(max = 500, message = "description length must not exceed 500")
    @Schema(description = "Task description")
    private String description;

    @Size(max = 500, message = "remark length must not exceed 500")
    @Schema(description = "Remark")
    private String remark;
}
