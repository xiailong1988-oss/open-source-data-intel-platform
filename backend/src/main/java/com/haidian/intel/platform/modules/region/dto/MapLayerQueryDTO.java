package com.haidian.intel.platform.modules.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 地图图层查询参数。
 * 支持按图层类型和启用状态做基础过滤，便于前端联调时按需取数。
 */
@Getter
@Setter
@Schema(description = "地图图层查询参数")
public class MapLayerQueryDTO {

    @Schema(description = "是否只返回启用中的图层", example = "true", defaultValue = "true")
    private Boolean enabledOnly = true;

    @Size(max = 64, message = "图层类型长度不能超过64个字符")
    @Schema(description = "图层类型", example = "event")
    private String layerType;
}
