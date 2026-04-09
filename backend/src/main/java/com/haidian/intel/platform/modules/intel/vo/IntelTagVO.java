package com.haidian.intel.platform.modules.intel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 情报标签返回对象。
 */
@Getter
@Setter
@Schema(description = "情报标签")
public class IntelTagVO {

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签类型")
    private String tagType;

    @Schema(description = "排序号")
    private Integer sortNo;
}
