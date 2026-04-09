package com.haidian.intel.platform.modules.packagecenter.dto;

import com.haidian.intel.platform.common.constant.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Base DTO for data package create and update.
 */
@Getter
@Setter
public class DataPackageSaveDTO {

    @NotBlank(message = "packageName must not be blank")
    @Size(max = 255, message = "packageName length must not exceed 255")
    @Schema(description = "Data package name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String packageName;

    @NotBlank(message = "packageType must not be blank")
    @Size(max = 64, message = "packageType length must not exceed 64")
    @Schema(description = "Data package type code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String packageType;

    @NotBlank(message = "statMonth must not be blank")
    @Pattern(
            regexp = ValidationPatterns.YEAR_MONTH,
            message = "statMonth must match yyyy-MM"
    )
    @Schema(description = "Statistic month, such as 2026-04", requiredMode = Schema.RequiredMode.REQUIRED)
    private String statMonth;

    @NotBlank(message = "regionScope must not be blank")
    @Size(max = 255, message = "regionScope length must not exceed 255")
    @Schema(description = "Region scope text", requiredMode = Schema.RequiredMode.REQUIRED)
    private String regionScope;

    @Size(max = 1000, message = "description length must not exceed 1000")
    @Schema(description = "Package description")
    private String description;

    @Size(max = 500, message = "remark length must not exceed 500")
    @Schema(description = "Remark")
    private String remark;

    @Valid
    @Schema(description = "Package file manifest list")
    private List<DataPackageFileSaveDTO> files = new ArrayList<>();
}
