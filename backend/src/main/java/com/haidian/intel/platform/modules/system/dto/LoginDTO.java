package com.haidian.intel.platform.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Login request DTO.
 */
@Getter
@Setter
@Schema(description = "Login DTO")
public class LoginDTO {

    @NotBlank(message = "username must not be blank")
    @Size(max = 64, message = "username length must not exceed 64")
    @Schema(description = "Login username", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "password must not be blank")
    @Size(max = 128, message = "password length must not exceed 128")
    @Schema(description = "Login password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
