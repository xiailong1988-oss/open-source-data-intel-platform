package com.haidian.intel.platform.modules.packagecenter.dto;

import com.haidian.intel.platform.common.constant.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Base DTO for delivery record create requests.
 */
@Getter
@Setter
public class DeliveryRecordSaveDTO {

    @NotBlank(message = "deliveryType must not be blank")
    @Size(max = 64, message = "deliveryType length must not exceed 64")
    @Schema(description = "Delivery type code", defaultValue = "data_package")
    private String deliveryType = "data_package";

    @NotNull(message = "relatedId must not be null")
    @Positive(message = "relatedId must be greater than 0")
    @Schema(description = "Related business ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long relatedId;

    @NotBlank(message = "receiverName must not be blank")
    @Size(max = 128, message = "receiverName length must not exceed 128")
    @Schema(description = "Receiver name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverName;

    @Size(max = 255, message = "receiverOrg length must not exceed 255")
    @Schema(description = "Receiver organization")
    private String receiverOrg;

    @Size(max = 64, message = "contactPhone length must not exceed 64")
    @Pattern(
            regexp = ValidationPatterns.OPTIONAL_PHONE_NUMBER,
            message = "contactPhone must be a valid phone number"
    )
    @Schema(description = "Receiver contact phone")
    private String contactPhone;

    @NotNull(message = "deliveryTime must not be null")
    @Schema(description = "Delivery time", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime deliveryTime;

    @NotBlank(message = "deliveryMethod must not be blank")
    @Size(max = 64, message = "deliveryMethod length must not exceed 64")
    @Schema(description = "Delivery method code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deliveryMethod;

    @Size(max = 1000, message = "description length must not exceed 1000")
    @Schema(description = "Delivery description")
    private String description;

    @Size(max = 500, message = "remark length must not exceed 500")
    @Schema(description = "Remark")
    private String remark;
}
