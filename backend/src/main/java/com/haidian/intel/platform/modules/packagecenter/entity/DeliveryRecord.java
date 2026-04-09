package com.haidian.intel.platform.modules.packagecenter.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Delivery record entity for package delivery management.
 */
@Getter
@Setter
@TableName("biz_delivery_record")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Delivery record entity")
public class DeliveryRecord extends BaseEntity {

    @Schema(description = "Delivery type code")
    private String deliveryType;

    @Schema(description = "Related business ID")
    private Long relatedId;

    @Schema(description = "Related business name snapshot")
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

    @Schema(description = "Delivery description")
    private String description;
}
