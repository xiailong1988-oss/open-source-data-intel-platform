package com.haidian.intel.platform.modules.region.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页地图点位返回对象。
 * 返回结构优先贴合前端地图渲染和详情跳转的直接消费场景。
 */
@Getter
@Setter
@Schema(description = "首页地图点位")
public class MapPointVO {

    @Schema(description = "点位ID，同时可作为详情ID")
    private Long id;

    @Schema(description = "情报标题")
    private String title;

    @Schema(description = "地区ID")
    private Long regionId;

    @Schema(description = "地区名称")
    private String region;

    @Schema(description = "事件类型ID")
    private Long eventTypeId;

    @Schema(description = "事件类型名称")
    private String type;

    @Schema(description = "事件分类")
    private String category;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "坐标数组，顺序为[lng, lat]")
    private List<BigDecimal> coords;

    @Schema(description = "热度")
    private Integer heat;

    @Schema(description = "重要级别")
    private Integer importanceLevel;

    @Schema(description = "风险等级")
    private Integer riskLevel;

    @Schema(description = "是否重点关注：1是 0否")
    private Integer focusFlag;

    @Schema(description = "点位时间，优先取事件发生时间")
    private LocalDateTime time;

    @Schema(description = "摘要")
    private String summary;
}
