package com.haidian.intel.platform.modules.collect.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Collection task entity for admin task management.
 */
@Getter
@Setter
@TableName("biz_collection_task")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Collection task entity")
public class CollectionTask extends BaseEntity {

    @Schema(description = "Task name")
    private String taskName;

    @Schema(description = "Collection source ID")
    private Long sourceId;

    @Schema(description = "Task type code")
    private String taskType;

    @Schema(description = "Cron expression or schedule rule")
    private String cronExpr;

    @Schema(description = "Human readable frequency")
    private String frequencyDesc;

    @Schema(description = "Enable flag")
    private Integer enabled;

    @Schema(description = "Run status code")
    private String runStatus;

    @Schema(description = "Latest run result code")
    private String latestResult;

    @Schema(description = "Latest run time")
    private LocalDateTime latestRunTime;

    @Schema(description = "Collected record count")
    private Long recordCount;

    @Schema(description = "Success rate")
    private BigDecimal successRate;

    @Schema(description = "Task description")
    private String description;
}
