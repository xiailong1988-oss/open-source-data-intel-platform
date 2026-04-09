package com.haidian.intel.platform.modules.system.service;

import com.haidian.intel.platform.common.auth.AuthenticatedUser;
import com.haidian.intel.platform.modules.system.dto.LoginDTO;
import com.haidian.intel.platform.modules.system.vo.CurrentUserVO;
import com.haidian.intel.platform.modules.system.vo.LoginVO;

/**
 * Service for lightweight authentication and current user access.
 */
public interface AuthService {

    /**
     * Login and create a token session.
     *
     * @param loginDTO login request
     * @return login response
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * Logout current token session.
     *
     * @param token access token
     */
    void logout(String token);

    /**
     * Get current authenticated user info.
     *
     * @return current user info
     */
    CurrentUserVO getCurrentUser();

    /**
     * Resolve token to authenticated user snapshot.
     *
     * @param token access token
     * @return authenticated user or null if not found/expired
     */
    AuthenticatedUser resolveToken(String token);
}
