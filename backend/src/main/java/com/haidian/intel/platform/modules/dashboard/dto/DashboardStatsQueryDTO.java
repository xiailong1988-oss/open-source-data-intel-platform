package com.haidian.intel.platform.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 首页概览与地区态势摘要通用查询参数。
 * 保持和首页地区切换、事件类型切换、时间范围联动一致。
 */
@Getter
@Setter
@Schema(description = "首页概览统计查询参数")
public class DashboardStatsQueryDTO {

    @Positive(message = "地区ID必须大于0")
    @Schema(description = "地区ID")
    private Long regionId;

    @Positive(message = "事件类型ID必须大于0")
    @Schema(description = "事件类型ID")
    private Long eventTypeId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
