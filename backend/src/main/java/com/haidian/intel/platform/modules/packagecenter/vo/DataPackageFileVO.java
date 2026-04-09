package com.haidian.intel.platform.modules.packagecenter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * File list VO for data package file manifests.
 */
@Getter
@Setter
@Schema(description = "Data package file VO")
public class DataPackageFileVO {

    @Schema(description = "Package file ID")
    private Long id;

    @Schema(description = "Data package ID")
    private Long packageId;

    @Schema(description = "File name")
    private String fileName;

    @Schema(description = "Logical file path")
    private String filePath;

    @Schema(description = "File size")
    private Long fileSize;

    @Schema(description = "File type")
    private String fileType;

    @Schema(description = "File checksum")
    private String checksum;

    @Schema(description = "Reserved storage type")
    private String storageType;

    @Schema(description = "Reserved storage bucket")
    private String storageBucket;

    @Schema(description = "Reserved storage object key")
    private String storageObjectKey;

    @Schema(description = "Reserved download URL")
    private String downloadUrl;

    @Schema(description = "Display order")
    private Integer sortOrder;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Created time")
    private LocalDateTime createdTime;

    @Schema(description = "Updated time")
    private LocalDateTime updatedTime;
}
