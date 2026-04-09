package com.haidian.intel.platform.modules.intel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页快讯条返回对象。
 * 字段按首页横向播报直接绑定的最小集合输出。
 */
@Getter
@Setter
@Schema(description = "首页快讯条")
public class IntelTickerVO {

    @Schema(description = "详情ID")
    private Long detailId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "时间，当前取情报发布时间")
    private LocalDateTime time;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "标签名称列表")
    private List<String> tags;
}
