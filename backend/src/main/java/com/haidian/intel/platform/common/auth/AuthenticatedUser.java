package com.haidian.intel.platform.common.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Authenticated user snapshot stored in request context and token session.
 */
@Getter
@Setter
@Schema(description = "Authenticated user snapshot")
public class AuthenticatedUser {

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Login username")
    private String username;

    @Schema(description = "Display name")
    private String realName;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Current token")
    private String token;

    @Schema(description = "Token expire time")
    private LocalDateTime expiresAt;

    @Schema(description = "Role code list")
    private List<String> roleCodes;

    @Schema(description = "Role name list")
    private List<String> roleNames;
}
