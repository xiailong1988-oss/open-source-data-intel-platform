package com.haidian.intel.platform.modules.intel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 情报标签实体。
 * 对应 biz_intel_tag，用于情报打标和标签联查。
 */
@Getter
@Setter
@TableName("biz_intel_tag")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "情报标签实体")
public class IntelTag extends BaseEntity {

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签类型")
    private String tagType;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "是否启用：1启用 0停用")
    private Integer enabled;
}
