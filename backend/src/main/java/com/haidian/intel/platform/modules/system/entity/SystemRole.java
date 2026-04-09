package com.haidian.intel.platform.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * System role entity for lightweight role control.
 */
@Getter
@Setter
@TableName("sys_role")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "System role entity")
public class SystemRole extends BaseEntity {

    @Schema(description = "Role code")
    private String roleCode;

    @Schema(description = "Role name")
    private String roleName;

    @Schema(description = "Enable status")
    private Integer status;
}
