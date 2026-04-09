package com.haidian.intel.platform.modules.region.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * 地区详情返回对象。
 * 用于首页地区切换后的基础信息和地图视角参数读取。
 */
@Getter
@Setter
@Schema(description = "地区详情")
public class RegionDetailVO {

    @Schema(description = "地区 ID")
    private Long id;

    @Schema(description = "父级地区 ID")
    private Long parentId;

    @Schema(description = "地区编码")
    private String regionCode;

    @Schema(description = "地区名称")
    private String regionName;

    @Schema(description = "地区层级")
    private Integer regionLevel;

    @Schema(description = "地区类型")
    private String regionType;

    @Schema(description = "中心经度")
    private BigDecimal centerLng;

    @Schema(description = "中心纬度")
    private BigDecimal centerLat;

    @Schema(description = "默认缩放级别")
    private Integer zoomLevel;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "是否启用：1 启用 0 停用")
    private Integer enabled;

    @Schema(description = "是否存在边界数据")
    private Boolean hasGeoBoundary;
}
