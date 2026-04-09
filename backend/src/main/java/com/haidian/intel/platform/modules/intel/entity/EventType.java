package com.haidian.intel.platform.modules.intel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 事件类型实体。
 * 复用 biz_event_type，为情报分页和详情查询回填事件类型信息。
 */
@Getter
@Setter
@TableName("biz_event_type")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "事件类型实体")
public class EventType extends BaseEntity {

    @Schema(description = "事件类型编码")
    private String eventCode;

    @Schema(description = "事件类型名称")
    private String eventName;

    @Schema(description = "事件分类")
    private String eventCategory;

    @Schema(description = "展示色值")
    private String colorCode;

    @Schema(description = "图标编码")
    private String iconCode;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "是否启用：1启用 0停用")
    private Integer enabled;
}
