package com.haidian.intel.platform.modules.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 地区态势摘要返回对象。
 */
@Getter
@Setter
@Schema(description = "地区态势摘要")
public class DashboardRegionSummaryVO {

    @Schema(description = "地区ID")
    private Long regionId;

    @Schema(description = "地区名称")
    private String regionName;

    @Schema(description = "一句话态势摘要")
    private String summary;

    @Schema(description = "总事件数")
    private Long totalEventCount;

    @Schema(description = "风险事件数")
    private Long riskEventCount;

    @Schema(description = "突发事件数")
    private Long emergencyEventCount;

    @Schema(description = "热点事件数")
    private Long hotspotEventCount;

    @Schema(description = "重点关注数")
    private Long focusCount;

    @Schema(description = "最近更新时间")
    private LocalDateTime latestUpdateTime;
}
