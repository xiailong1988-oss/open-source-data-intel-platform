package com.haidian.intel.platform.modules.system.service;

import com.haidian.intel.platform.common.auth.AuthenticatedUser;

/**
 * Service for managing in-memory token sessions.
 */
public interface AuthTokenService {

    /**
     * Create a token session for the given authenticated user snapshot.
     *
     * @param authenticatedUser authenticated user snapshot
     * @return persisted session snapshot
     */
    AuthenticatedUser createSession(AuthenticatedUser authenticatedUser);

    /**
     * Resolve token to a valid session snapshot.
     *
     * @param token access token
     * @return valid session or null
     */
    AuthenticatedUser getSession(String token);

    /**
     * Remove a token session.
     *
     * @param token access token
     */
    void removeSession(String token);
}
