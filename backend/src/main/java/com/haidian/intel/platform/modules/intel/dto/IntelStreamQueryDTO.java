package com.haidian.intel.platform.modules.intel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页主情报流查询参数。
 * 当前仅保留首页联调必需的地区、事件类型、关键词和分页能力。
 */
@Getter
@Setter
@Schema(description = "首页主情报流查询参数")
public class IntelStreamQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数不能超过100")
    @Schema(description = "每页条数", example = "10", defaultValue = "10")
    private Long pageSize = 10L;

    @Positive(message = "地区ID必须大于0")
    @Schema(description = "地区ID")
    private Long regionId;

    @Positive(message = "事件类型ID必须大于0")
    @Schema(description = "事件类型ID")
    private Long eventTypeId;

    @Schema(description = "关键词，支持标题/摘要/地址基础过滤")
    private String keyword;
}
