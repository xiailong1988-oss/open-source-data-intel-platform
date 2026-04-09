package com.haidian.intel.platform.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Login response VO.
 */
@Getter
@Setter
@Schema(description = "Login response VO")
public class LoginVO {

    @Schema(description = "Access token")
    private String accessToken;

    @Schema(description = "Token type")
    private String tokenType;

    @Schema(description = "Token expire time")
    private LocalDateTime expiresAt;

    @Schema(description = "Current user info")
    private CurrentUserVO currentUser;
}
