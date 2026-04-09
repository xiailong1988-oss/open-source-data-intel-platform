package com.haidian.intel.platform.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 健康检查响应对象。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用健康状态")
public class HealthStatusVO {

    @Schema(description = "应用名称", example = "haidian-intel-platform-backend")
    private String application;

    @Schema(description = "应用状态", example = "UP")
    private String status;

    @Schema(description = "当前激活环境", example = "dev")
    private String activeProfile;

    @Schema(description = "服务端时间", example = "2026-04-07T16:00:00")
    private LocalDateTime serverTime;
}
