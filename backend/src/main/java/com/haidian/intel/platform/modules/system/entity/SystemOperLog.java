package com.haidian.intel.platform.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Operation log entity for automatic audit recording.
 */
@Getter
@Setter
@TableName("sys_oper_log")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "System operation log entity")
public class SystemOperLog extends BaseEntity {

    @Schema(description = "Module name")
    private String moduleName;

    @Schema(description = "Action name")
    private String actionName;

    @Schema(description = "Operator ID")
    private Long operatorId;

    @Schema(description = "Operator name")
    private String operatorName;

    @Schema(description = "Request URI")
    private String requestUri;

    @Schema(description = "Request method")
    private String requestMethod;

    @Schema(description = "Request parameters")
    private String requestParam;

    @Schema(description = "Response data")
    private String responseData;

    @Schema(description = "Success flag")
    private Integer successFlag;

    @Schema(description = "Operation time")
    private LocalDateTime operateTime;
}
