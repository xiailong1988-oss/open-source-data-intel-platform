package com.haidian.intel.platform.modules.intel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 情报主表实体。
 * 对应 biz_intel_item，是后续快讯条、情报流、详情页和地图点位的基础数据载体。
 */
@Getter
@Setter
@TableName("biz_intel_item")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "情报主表实体")
public class IntelItem extends BaseEntity {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "所属地区ID")
    private Long regionId;

    @Schema(description = "事件类型ID")
    private Long eventTypeId;

    @Schema(description = "来源ID")
    private Long sourceId;

    @Schema(description = "重要级别")
    private Integer importanceLevel;

    @Schema(description = "热度分值")
    private Integer heatScore;

    @Schema(description = "是否重点关注：1是 0否")
    private Integer focusFlag;

    @Schema(description = "风险等级")
    private Integer riskLevel;

    @Schema(description = "事件发生时间")
    private LocalDateTime occurTime;

    @Schema(description = "信息发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "地址描述")
    private String address;

    @Schema(description = "数据状态：1正常 0停用")
    private Integer status;

    @Schema(description = "审核状态：0待审核 1已通过 2已驳回")
    private Integer reviewStatus;

    @Schema(description = "数据来源方式")
    private String dataOrigin;

    @Schema(description = "扩展字段 JSON")
    private String extJson;
}
