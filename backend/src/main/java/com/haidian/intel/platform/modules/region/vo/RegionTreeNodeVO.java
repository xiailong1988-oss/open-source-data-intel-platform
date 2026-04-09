package com.haidian.intel.platform.modules.region.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 地区树节点返回对象。
 * 该结构直接面向前端地区切换树使用，包含基础坐标和子节点列表。
 */
@Getter
@Setter
@Schema(description = "地区树节点")
public class RegionTreeNodeVO {

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

    @Schema(description = "子节点列表")
    private List<RegionTreeNodeVO> children = new ArrayList<>();
}
