package com.haidian.intel.platform.modules.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页重点关注对象返回对象。
 */
@Getter
@Setter
@Schema(description = "首页重点关注对象")
public class DashboardFocusTargetVO {

    @Schema(description = "关注对象ID")
    private Long id;

    @Schema(description = "关注对象标题")
    private String title;

    @Schema(description = "关注对象类型")
    private String targetType;

    @Schema(description = "地区ID")
    private Long regionId;

    @Schema(description = "地区名称")
    private String regionName;

    @Schema(description = "关注等级")
    private Integer level;

    @Schema(description = "关注等级标签")
    private String levelLabel;

    @Schema(description = "最近事件时间")
    private LocalDateTime latestEventTime;

    @Schema(description = "最近事件标题")
    private String latestEventTitle;

    @Schema(description = "当前状态")
    private String status;

    @Schema(description = "检索关键词")
    private String keyword;

    @Schema(description = "关注摘要")
    private String summary;

    @Schema(description = "关联详情ID")
    private Long detailId;
}
