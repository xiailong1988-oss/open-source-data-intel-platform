package com.haidian.intel.platform.modules.packagecenter.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Detail VO for delivery record management.
 */
@Getter
@Setter
@Schema(description = "Delivery record detail VO")
public class DeliveryRecordDetailVO {

    @Schema(description = "Delivery record ID")
    private Long id;

    @Schema(description = "Delivery type code")
    private String deliveryType;

    @Schema(description = "Delivery type label")
    private String deliveryTypeLabel;

    @Schema(description = "Related business ID")
    private Long relatedId;

    @Schema(description = "Related business name")
    private String relatedName;

    @Schema(description = "Related business version snapshot")
    private Integer relatedVersion;

    @Schema(description = "Receiver name")
    private String receiverName;

    @Schema(description = "Receiver organization")
    private String receiverOrg;

    @Schema(description = "Receiver contact phone")
    private String contactPhone;

    @Schema(description = "Delivery time")
    private LocalDateTime deliveryTime;

    @Schema(description = "Delivery method code")
    private String deliveryMethod;

    @Schema(description = "Delivery method label")
    private String deliveryMethodLabel;

    @Schema(description = "Delivery description")
    private String description;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Created time")
    private LocalDateTime createdTime;

    @Schema(description = "Updated time")
    private LocalDateTime updatedTime;
}
