package com.haidian.intel.platform.modules.region.vo;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 地区边界返回对象。
 * 同时返回原始 GeoJSON 和简化 GeoJSON，方便前端按不同场景直接使用。
 */
@Getter
@Setter
@Schema(description = "地区边界信息")
public class RegionGeoVO {

    @Schema(description = "地区 ID")
    private Long regionId;

    @Schema(description = "地区编码")
    private String regionCode;

    @Schema(description = "地区名称")
    private String regionName;

    @Schema(description = "原始 GeoJSON")
    private JsonNode geoJson;

    @Schema(description = "简化版 GeoJSON")
    private JsonNode simplifiedGeoJson;

    @Schema(description = "边界版本号")
    private Integer versionNo;

    @Schema(description = "边界状态：1 启用 0 停用")
    private Integer status;
}
