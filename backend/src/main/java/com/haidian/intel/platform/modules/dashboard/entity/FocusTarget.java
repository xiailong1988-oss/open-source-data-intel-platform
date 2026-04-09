package com.haidian.intel.platform.modules.dashboard.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 首页重点关注对象实体。
 * 当前用于首页重点关注对象列表展示与联调。
 */
@Getter
@Setter
@TableName("biz_focus_target")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "首页重点关注对象实体")
public class FocusTarget extends BaseEntity {

    @Schema(description = "关注对象名称")
    private String targetName;

    @Schema(description = "关注对象类型")
    private String targetType;

    @Schema(description = "地区ID")
    private Long regionId;

    @Schema(description = "关注等级：3高 2中 1低")
    private Integer focusLevel;

    @Schema(description = "最近事件时间")
    private LocalDateTime latestEventTime;

    @Schema(description = "关联最新情报ID")
    private Long latestIntelId;

    @Schema(description = "最近事件标题")
    private String latestEventTitle;

    @Schema(description = "关注状态")
    private String status;

    @Schema(description = "检索关键词")
    private String keyword;

    @Schema(description = "关注摘要")
    private String summary;

    @Schema(description = "是否启用：1启用 0停用")
    private Integer enabled;
}
