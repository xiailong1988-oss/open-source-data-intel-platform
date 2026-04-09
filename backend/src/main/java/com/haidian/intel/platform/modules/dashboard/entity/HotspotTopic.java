package com.haidian.intel.platform.modules.dashboard.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页热点专题实体。
 * 对应首页右侧或中部的热点专题展示数据。
 */
@Getter
@Setter
@TableName("biz_hotspot_topic")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "首页热点专题实体")
public class HotspotTopic extends BaseEntity {

    @Schema(description = "专题名称")
    private String topicName;

    @Schema(description = "专题分类")
    private String topicCategory;

    @Schema(description = "地区ID")
    private Long regionId;

    @Schema(description = "热度分值")
    private Integer heatScore;

    @Schema(description = "提及次数")
    private Integer mentionCount;

    @Schema(description = "最近事件时间")
    private LocalDateTime latestEventTime;

    @Schema(description = "关联最新情报ID")
    private Long latestIntelId;

    @Schema(description = "归属部门")
    private String ownerDept;

    @Schema(description = "专题关键词")
    private String keyword;

    @Schema(description = "专题摘要")
    private String summary;

    @Schema(description = "是否启用：1启用 0停用")
    private Integer enabled;
}
