package com.haidian.intel.platform.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * System user-role relation entity.
 */
@Getter
@Setter
@TableName("sys_user_role")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "System user role relation entity")
public class SystemUserRole extends BaseEntity {

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Role ID")
    private Long roleId;
}
