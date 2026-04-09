package com.haidian.intel.platform.modules.system.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.common.auth.LoginRequired;
import com.haidian.intel.platform.common.auth.UserContextHolder;
import com.haidian.intel.platform.modules.system.dto.LoginDTO;
import com.haidian.intel.platform.modules.system.service.AuthService;
import com.haidian.intel.platform.modules.system.vo.CurrentUserVO;
import com.haidian.intel.platform.modules.system.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for basic authentication endpoints.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ApiResponse.success(authService.login(loginDTO));
    }

    @PostMapping("/logout")
    @LoginRequired
    @Operation(summary = "Logout")
    public ApiResponse<Void> logout() {
        String token = UserContextHolder.getUser() == null ? null : UserContextHolder.getUser().getToken();
        authService.logout(token);
        return ApiResponse.success(null);
    }

    @GetMapping("/current-user")
    @LoginRequired
    @Operation(summary = "Get current user")
    public ApiResponse<CurrentUserVO> getCurrentUser() {
        return ApiResponse.success(authService.getCurrentUser());
    }
}
