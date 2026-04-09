package com.haidian.intel.platform.modules.region.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 地图图层配置实体。
 * 对应 biz_map_layer，用于地图图层列表和样式配置查询。
 */
@Getter
@Setter
@TableName("biz_map_layer")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "地图图层实体")
public class MapLayer extends BaseEntity {

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

    @Schema(description = "扩展样式配置 JSON")
    private String extJson;
}
