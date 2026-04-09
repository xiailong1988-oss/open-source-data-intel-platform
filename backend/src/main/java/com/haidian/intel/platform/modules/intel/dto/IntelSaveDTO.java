package com.haidian.intel.platform.modules.intel.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 情报保存基础 DTO。
 * 创建和修改情报共用这组字段，保持入参结构一致，便于后续扩展。
 */
@Getter
@Setter
@Schema(description = "情报保存基础参数")
public abstract class IntelSaveDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 500, message = "标题长度不能超过500个字符")
    @Schema(description = "标题")
    private String title;

    @NotBlank(message = "摘要不能为空")
    @Schema(description = "摘要")
    private String summary;

    @NotBlank(message = "正文内容不能为空")
    @Schema(description = "正文内容")
    private String content;

    @NotNull(message = "地区ID不能为空")
    @Positive(message = "地区ID必须大于0")
    @Schema(description = "地区ID")
    private Long regionId;

    @NotNull(message = "事件类型ID不能为空")
    @Positive(message = "事件类型ID必须大于0")
    @Schema(description = "事件类型ID")
    private Long eventTypeId;

    @NotNull(message = "来源ID不能为空")
    @Positive(message = "来源ID必须大于0")
    @Schema(description = "来源ID")
    private Long sourceId;

    @Schema(description = "重要级别", example = "3")
    private Integer importanceLevel;

    @Schema(description = "热度分值", example = "80")
    private Integer heatScore;

    @Max(value = 1, message = "是否重点关注只能为0或1")
    @Schema(description = "是否重点关注：1是 0否", example = "0")
    private Integer focusFlag;

    @Schema(description = "风险等级", example = "2")
    private Integer riskLevel;

    @NotNull(message = "事件发生时间不能为空")
    @Schema(description = "事件发生时间", example = "2026-04-07 10:00:00")
    private LocalDateTime occurTime;

    @NotNull(message = "信息发布时间不能为空")
    @Schema(description = "信息发布时间", example = "2026-04-07 10:30:00")
    private LocalDateTime publishTime;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Size(max = 255, message = "地址长度不能超过255个字符")
    @Schema(description = "地址描述")
    private String address;

    @Max(value = 1, message = "数据状态只能为0或1")
    @Schema(description = "数据状态：1正常 0停用", example = "1")
    private Integer status;

    @Max(value = 2, message = "审核状态只能为0、1、2")
    @Schema(description = "审核状态：0待审核 1已通过 2已驳回", example = "1")
    private Integer reviewStatus;

    @Size(max = 64, message = "数据来源方式长度不能超过64个字符")
    @Schema(description = "数据来源方式", example = "manual")
    private String dataOrigin;

    @Schema(description = "扩展字段")
    private JsonNode extJson;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "关联标签ID列表")
    private List<@Positive(message = "标签ID必须大于0") Long> tagIds;
}
