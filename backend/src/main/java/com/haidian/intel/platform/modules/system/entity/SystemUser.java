package com.haidian.intel.platform.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * System user entity for basic authentication.
 */
@Getter
@Setter
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "System user entity")
public class SystemUser extends BaseEntity {

    @Schema(description = "Login username")
    private String username;

    @Schema(description = "Password hash")
    private String password;

    @Schema(description = "Real name")
    private String realName;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Enable status")
    private Integer status;

    @Schema(description = "Last login time")
    private LocalDateTime lastLoginTime;
}
