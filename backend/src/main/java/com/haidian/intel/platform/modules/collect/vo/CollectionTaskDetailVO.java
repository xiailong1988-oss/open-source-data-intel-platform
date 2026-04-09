package com.haidian.intel.platform.modules.collect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Detail VO for collection task management.
 */
@Getter
@Setter
@Schema(description = "Collection task detail VO")
public class CollectionTaskDetailVO {

    @Schema(description = "Task ID")
    private Long id;

    @Schema(description = "Task name")
    private String taskName;

    @Schema(description = "Collection source ID")
    private Long sourceId;

    @Schema(description = "Collection source name")
    private String sourceName;

    @Schema(description = "Task type code")
    private String taskType;

    @Schema(description = "Task type label")
    private String taskTypeLabel;

    @Schema(description = "Cron expression or schedule rule")
    private String cronExpr;

    @Schema(description = "Human readable frequency")
    private String frequencyDesc;

    @Schema(description = "Enable flag")
    private Integer enabled;

    @Schema(description = "Run status code")
    private String runStatus;

    @Schema(description = "Run status label")
    private String runStatusLabel;

    @Schema(description = "Derived task status code")
    private String status;

    @Schema(description = "Derived task status label")
    private String statusLabel;

    @Schema(description = "Latest run result code")
    private String latestResult;

    @Schema(description = "Latest run result label")
    private String latestResultLabel;

    @Schema(description = "Latest run time")
    private LocalDateTime latestRunTime;

    @Schema(description = "Collected record count")
    private Long recordCount;

    @Schema(description = "Success rate")
    private BigDecimal successRate;

    @Schema(description = "Task description")
    private String description;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Created time")
    private LocalDateTime createdTime;

    @Schema(description = "Updated time")
    private LocalDateTime updatedTime;
}
