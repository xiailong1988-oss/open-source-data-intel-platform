package com.haidian.intel.platform.modules.intel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 情报来源实体。
 * 对应 biz_intel_source，用于情报创建和详情查询时回填来源信息。
 */
@Getter
@Setter
@TableName("biz_intel_source")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "情报来源实体")
public class IntelSource extends BaseEntity {

    @Schema(description = "来源名称")
    private String sourceName;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "来源地址")
    private String sourceUrl;

    @Schema(description = "可信度等级")
    private Integer credibilityLevel;

    @Schema(description = "是否启用：1启用 0停用")
    private Integer enabled;
}
