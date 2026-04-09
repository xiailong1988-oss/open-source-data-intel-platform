package com.haidian.intel.platform.modules.region.vo;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 地图图层返回对象。
 * 样式配置会被解析成结构化 JSON，方便前端直接挂到地图图层配置中。
 */
@Getter
@Setter
@Schema(description = "地图图层配置")
public class MapLayerVO {

    @Schema(description = "图层 ID")
    private Long id;

    @Schema(description = "图层编码")
    private String layerCode;

    @Schema(description = "图层名称")
    private String layerName;

    @Schema(description = "图层类型")
    private String layerType;

    @Schema(description = "是否启用：1 启用 0 停用")
    private Integer enabled;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "图层样式配置")
    private JsonNode styleConfig;
}
