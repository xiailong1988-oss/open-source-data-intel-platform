package com.haidian.intel.platform.modules.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 地图点位查询参数。
 * 当前只保留首页地图联调所需的地区、事件类型和时间范围过滤能力。
 */
@Getter
@Setter
@Schema(description = "地图点位查询参数")
public class MapPointQueryDTO {

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
