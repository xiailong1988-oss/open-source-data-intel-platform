package com.haidian.intel.platform.modules.intel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 情报标签关联实体。
 * 对应 biz_intel_item_tag，用于情报与标签的多对多关系维护。
 */
@Getter
@Setter
@TableName("biz_intel_item_tag")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "情报标签关联实体")
public class IntelItemTag extends BaseEntity {

    @Schema(description = "情报ID")
    private Long intelItemId;

    @Schema(description = "标签ID")
    private Long tagId;
}
