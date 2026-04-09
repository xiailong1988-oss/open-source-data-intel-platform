package com.haidian.intel.platform.modules.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页热点专题查询参数。
 * 当前只保留首页联调必要的地区过滤和数量控制。
 */
@Getter
@Setter
@Schema(description = "首页热点专题查询参数")
public class DashboardHotspotQueryDTO {

    @Positive(message = "地区ID必须大于0")
    @Schema(description = "地区ID")
    private Long regionId;

    @Min(value = 1, message = "返回条数最小为1")
    @Max(value = 20, message = "返回条数不能超过20")
    @Schema(description = "返回条数", example = "6", defaultValue = "6")
    private Integer limit = 6;
}
