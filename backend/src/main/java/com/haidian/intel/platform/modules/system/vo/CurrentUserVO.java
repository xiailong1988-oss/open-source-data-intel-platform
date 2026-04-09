package com.haidian.intel.platform.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Current user response VO.
 */
@Getter
@Setter
@Schema(description = "Current user VO")
public class CurrentUserVO {

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Login username")
    private String username;

    @Schema(description = "Real name")
    private String realName;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Role code list")
    private List<String> roleCodes;

    @Schema(description = "Role name list")
    private List<String> roleNames;

    @Schema(description = "Token expire time")
    private LocalDateTime expiresAt;
}
