package com.haidian.intel.platform.modules.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页顶部概览统计返回对象。
 */
@Getter
@Setter
@Schema(description = "首页概览统计")
public class DashboardOverviewVO {

    @Schema(description = "地区ID")
    private Long regionId;

    @Schema(description = "地区名称")
    private String regionName;

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
