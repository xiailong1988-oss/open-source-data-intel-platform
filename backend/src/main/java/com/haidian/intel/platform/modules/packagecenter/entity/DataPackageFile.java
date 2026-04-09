package com.haidian.intel.platform.modules.packagecenter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Data package file entity for file manifest management.
 */
@Getter
@Setter
@TableName("biz_data_package_file")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Data package file entity")
public class DataPackageFile extends BaseEntity {

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
}
