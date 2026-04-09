package com.haidian.intel.platform.modules.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页热点专题返回对象。
 */
@Getter
@Setter
@Schema(description = "首页热点专题")
public class DashboardHotspotVO {

    @Schema(description = "专题ID")
    private Long id;

    @Schema(description = "专题标题")
    private String title;

    @Schema(description = "专题分类")
    private String category;

    @Schema(description = "地区ID")
    private Long regionId;

    @Schema(description = "地区名称")
    private String regionName;

    @Schema(description = "热度")
    private Integer heat;

    @Schema(description = "提及次数")
    private Integer mentions;

    @Schema(description = "最近更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "归属部门")
    private String department;

    @Schema(description = "专题关键词")
    private String keyword;

    @Schema(description = "专题摘要")
    private String summary;

    @Schema(description = "关联详情ID")
    private Long detailId;
}
