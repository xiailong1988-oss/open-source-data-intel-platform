package com.haidian.intel.platform.modules.packagecenter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Detail VO for data package management.
 */
@Getter
@Setter
@Schema(description = "Data package detail VO")
public class DataPackageDetailVO {

    @Schema(description = "Data package ID")
    private Long id;

    @Schema(description = "Data package name")
    private String packageName;

    @Schema(description = "Data package type code")
    private String packageType;

    @Schema(description = "Data package type label")
    private String packageTypeLabel;

    @Schema(description = "Statistic month")
    private String statMonth;

    @Schema(description = "Region scope text")
    private String regionScope;

    @Schema(description = "Package file count")
    private Integer fileCount;

    @Schema(description = "Package total size")
    private Long totalSize;

    @Schema(description = "Package status code")
    private String status;

    @Schema(description = "Package status label")
    private String statusLabel;

    @Schema(description = "Publish version")
    private Integer publishVersion;

    @Schema(description = "Latest publish time")
    private LocalDateTime publishTime;

    @Schema(description = "Latest publisher name")
    private String publishedByName;

    @Schema(description = "Latest publish note")
    private String publishNote;

    @Schema(description = "Package description")
    private String description;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Created time")
    private LocalDateTime createdTime;

    @Schema(description = "Updated time")
    private LocalDateTime updatedTime;
}
