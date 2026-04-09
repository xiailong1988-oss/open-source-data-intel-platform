package com.haidian.intel.platform.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 统一分页响应体。
 * 后续所有列表接口都按该结构返回，和前端分页参数约定保持一致。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一分页响应")
public class PageResponse<T> {

    @Schema(description = "当前页数据")
    private List<T> records;

    @Schema(description = "总记录数", example = "0")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum;

    @Schema(description = "每页条数", example = "20")
    private Long pageSize;

    public static <T> PageResponse<T> empty(long pageNum, long pageSize) {
        return new PageResponse<>(Collections.emptyList(), 0L, pageNum, pageSize);
    }
}
