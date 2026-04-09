package com.haidian.intel.platform.modules.region.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 地区边界实体。
 * 对应 biz_region_geo，用于向前端返回地区 GeoJSON 与简化边界。
 */
@Getter
@Setter
@TableName("biz_region_geo")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "地区边界实体")
public class RegionGeo extends BaseEntity {

    @Schema(description = "地区 ID")
    private Long regionId;

    @Schema(description = "原始 GeoJSON")
    private String geoJson;

    @Schema(description = "简化 GeoJSON")
    private String simplifiedGeoJson;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "状态：1 启用 0 停用")
    private Integer status;
}
