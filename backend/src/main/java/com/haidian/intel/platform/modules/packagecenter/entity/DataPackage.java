package com.haidian.intel.platform.modules.packagecenter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Data package entity for package management.
 */
@Getter
@Setter
@TableName("biz_data_package")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Data package entity")
public class DataPackage extends BaseEntity {

    @Schema(description = "Data package name")
    private String packageName;

    @Schema(description = "Data package type code")
    private String packageType;

    @Schema(description = "Statistic month, such as 2026-04")
    private String statMonth;

    @Schema(description = "Region scope text")
    private String regionScope;

    @Schema(description = "Package file count")
    private Integer fileCount;

    @Schema(description = "Package total size")
    private Long totalSize;

    @Schema(description = "Package status code")
    private String status;

    @Schema(description = "Latest publish version")
    private Integer publishVersion;

    @Schema(description = "Latest publish time")
    private LocalDateTime publishTime;

    @Schema(description = "Latest publisher name")
    private String publishedByName;

    @Schema(description = "Latest publish note")
    private String publishNote;

    @Schema(description = "Package description")
    private String description;
}
