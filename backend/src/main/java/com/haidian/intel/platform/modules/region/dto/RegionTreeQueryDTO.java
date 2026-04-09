package com.haidian.intel.platform.modules.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 地区树查询参数。
 * 当前默认只返回启用中的地区数据，方便首页直接渲染地区切换栏。
 */
@Getter
@Setter
@Schema(description = "地区树查询参数")
public class RegionTreeQueryDTO {

    @Schema(description = "是否只返回启用中的地区", example = "true", defaultValue = "true")
    private Boolean enabledOnly = true;
}
