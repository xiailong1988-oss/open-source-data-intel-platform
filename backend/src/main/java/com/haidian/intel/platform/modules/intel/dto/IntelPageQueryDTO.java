package com.haidian.intel.platform.modules.intel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 情报分页查询参数。
 * 当前支持地区、事件类型、时间范围和关键词过滤，后续可继续扩展更多维度。
 */
@Getter
@Setter
@Schema(description = "情报分页查询参数")
public class IntelPageQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数不能超过100")
    @Schema(description = "每页条数", example = "20", defaultValue = "20")
    private Long pageSize = 20L;

    @Positive(message = "地区ID必须大于0")
    @Schema(description = "地区ID")
    private Long regionId;

    @Positive(message = "事件类型ID必须大于0")
    @Schema(description = "事件类型ID")
    private Long eventTypeId;

    @Schema(description = "关键词，支持标题/摘要/地址基础过滤")
    private String keyword;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "数据状态：1正常 0停用")
    private Integer status;

    @Schema(description = "审核状态：0待审核 1已通过 2已驳回")
    private Integer reviewStatus;
}
