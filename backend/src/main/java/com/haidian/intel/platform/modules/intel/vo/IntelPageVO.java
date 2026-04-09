package com.haidian.intel.platform.modules.intel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 情报分页列表返回对象。
 * 面向列表场景输出核心摘要信息及来源、事件类型、标签等关联结果。
 */
@Getter
@Setter
@Schema(description = "情报分页列表项")
public class IntelPageVO {

    @Schema(description = "情报ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "地区ID")
    private Long regionId;

    @Schema(description = "地区名称")
    private String regionName;

    @Schema(description = "事件类型ID")
    private Long eventTypeId;

    @Schema(description = "事件类型名称")
    private String eventTypeName;

    @Schema(description = "事件分类")
    private String eventCategory;

    @Schema(description = "来源ID")
    private Long sourceId;

    @Schema(description = "来源名称")
    private String sourceName;

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

    @Schema(description = "数据状态")
    private Integer status;

    @Schema(description = "审核状态")
    private Integer reviewStatus;

    @Schema(description = "数据来源方式")
    private String dataOrigin;

    @Schema(description = "标签列表")
    private List<IntelTagVO> tags;
}
