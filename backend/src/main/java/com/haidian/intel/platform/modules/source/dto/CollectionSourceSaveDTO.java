package com.haidian.intel.platform.modules.source.dto;

import com.haidian.intel.platform.common.constant.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Base DTO for source create and update.
 */
@Getter
@Setter
public class CollectionSourceSaveDTO {

    @NotBlank(message = "sourceName must not be blank")
    @Size(max = 255, message = "sourceName length must not exceed 255")
    @Schema(description = "Source name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceName;

    @NotBlank(message = "sourceCategory must not be blank")
    @Size(max = 64, message = "sourceCategory length must not exceed 64")
    @Schema(description = "Source category code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceCategory;

    @NotBlank(message = "sourceUrl must not be blank")
    @Size(max = 500, message = "sourceUrl length must not exceed 500")
    @Pattern(
            regexp = ValidationPatterns.SCHEME_BASED_ADDRESS,
            message = "sourceUrl must include a scheme such as https://, jdbc:, mqtt:, or smb://"
    )
    @Schema(description = "Source endpoint or address", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceUrl;

    @NotBlank(message = "accessType must not be blank")
    @Size(max = 64, message = "accessType length must not exceed 64")
    @Schema(description = "Access type code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessType;

    @Min(value = 0, message = "enabled must be 0 or 1")
    @Max(value = 1, message = "enabled must be 0 or 1")
    @Schema(description = "Enable flag", defaultValue = "1")
    private Integer enabled = 1;

    @Size(max = 32, message = "status length must not exceed 32")
    @Schema(description = "Source status code", defaultValue = "running")
    private String status = "running";

    @Size(max = 255, message = "regionScope length must not exceed 255")
    @Schema(description = "Region scope text")
    private String regionScope;

    @NotBlank(message = "ownerName must not be blank")
    @Size(max = 128, message = "ownerName length must not exceed 128")
    @Schema(description = "Owner name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerName;

    @Schema(description = "Latest collect time")
    private LocalDateTime latestCollectTime;

    @Size(max = 500, message = "description length must not exceed 500")
    @Schema(description = "Source description")
    private String description;

    @Size(max = 500, message = "remark length must not exceed 500")
    @Schema(description = "Remark")
    private String remark;
}
