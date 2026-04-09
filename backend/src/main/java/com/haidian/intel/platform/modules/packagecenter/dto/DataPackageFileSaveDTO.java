package com.haidian.intel.platform.modules.packagecenter.dto;

import com.haidian.intel.platform.common.constant.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for saving file manifest items under a data package.
 */
@Getter
@Setter
@Schema(description = "Data package file save DTO")
public class DataPackageFileSaveDTO {

    @NotBlank(message = "fileName must not be blank")
    @Size(max = 255, message = "fileName length must not exceed 255")
    @Schema(description = "File name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @NotBlank(message = "filePath must not be blank")
    @Size(max = 500, message = "filePath length must not exceed 500")
    @Schema(description = "Logical file path", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filePath;

    @Min(value = 0, message = "fileSize must not be less than 0")
    @Schema(description = "File size in bytes", defaultValue = "0")
    private Long fileSize = 0L;

    @Size(max = 64, message = "fileType length must not exceed 64")
    @Schema(description = "File type")
    private String fileType;

    @Size(max = 128, message = "checksum length must not exceed 128")
    @Schema(description = "File checksum")
    private String checksum;

    @Size(max = 64, message = "storageType length must not exceed 64")
    @Schema(description = "Reserved storage type", defaultValue = "LOCAL")
    private String storageType = "LOCAL";

    @Size(max = 255, message = "storageBucket length must not exceed 255")
    @Schema(description = "Reserved storage bucket")
    private String storageBucket;

    @Size(max = 500, message = "storageObjectKey length must not exceed 500")
    @Schema(description = "Reserved storage object key")
    private String storageObjectKey;

    @Size(max = 500, message = "downloadUrl length must not exceed 500")
    @Pattern(
            regexp = ValidationPatterns.OPTIONAL_SCHEME_BASED_ADDRESS,
            message = "downloadUrl must include a scheme such as https:// or smb://"
    )
    @Schema(description = "Reserved download URL")
    private String downloadUrl;

    @Min(value = 0, message = "sortOrder must not be less than 0")
    @Schema(description = "Display order", defaultValue = "0")
    private Integer sortOrder = 0;

    @Size(max = 500, message = "remark length must not exceed 500")
    @Schema(description = "Remark")
    private String remark;
}
