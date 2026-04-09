package com.haidian.intel.platform.modules.intel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页快讯条查询参数。
 * 用于获取近期、高优先级、适合横向播报的情报数据。
 */
@Getter
@Setter
@Schema(description = "首页快讯条查询参数")
public class IntelTickerQueryDTO {

    @Positive(message = "地区ID必须大于0")
    @Schema(description = "地区ID")
    private Long regionId;

    @Positive(message = "事件类型ID必须大于0")
    @Schema(description = "事件类型ID")
    private Long eventTypeId;

    @Schema(description = "关键词，支持标题/摘要/地址基础过滤")
    private String keyword;

    @Min(value = 1, message = "快讯条数量最小为1")
    @Max(value = 30, message = "快讯条数量不能超过30")
    @Schema(description = "返回数量，默认10", example = "10", defaultValue = "10")
    private Integer limit = 10;
}
