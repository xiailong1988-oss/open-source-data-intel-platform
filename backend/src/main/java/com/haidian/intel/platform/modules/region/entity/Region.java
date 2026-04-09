package com.haidian.intel.platform.modules.region.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 地区主表实体。
 * 对应 biz_region，用于地区树、地区详情和地图联动基础信息查询。
 */
@Getter
@Setter
@TableName("biz_region")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "地区实体")
public class Region extends BaseEntity {

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
}
