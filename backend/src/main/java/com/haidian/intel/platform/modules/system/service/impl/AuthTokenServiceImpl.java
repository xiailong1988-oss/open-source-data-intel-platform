package com.haidian.intel.platform.modules.system.service.impl;

import com.haidian.intel.platform.common.auth.AuthenticatedUser;
import com.haidian.intel.platform.modules.system.service.AuthTokenService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * In-memory token session service for the current lightweight auth flow.
 */
@Service
public class AuthTokenServiceImpl implements AuthTokenService {

    private static final long TOKEN_EXPIRE_HOURS = 12L;
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, AuthenticatedUser> tokenStore = new ConcurrentHashMap<>();
    private final Map<Long, String> userTokenStore = new ConcurrentHashMap<>();

    @Override
    public AuthenticatedUser createSession(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.getUserId() == null) {
            return null;
        }

        String previousToken = userTokenStore.put(authenticatedUser.getUserId(), generateToken());
        if (previousToken != null) {
            tokenStore.remove(previousToken);
        }

        String token = userTokenStore.get(authenticatedUser.getUserId());
        authenticatedUser.setToken(token);
        authenticatedUser.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRE_HOURS));
        tokenStore.put(token, authenticatedUser);
        return authenticatedUser;
    }

    @Override
    public AuthenticatedUser getSession(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        AuthenticatedUser authenticatedUser = tokenStore.get(token);
        if (authenticatedUser == null) {
            return null;
        }
        if (authenticatedUser.getExpiresAt() != null && authenticatedUser.getExpiresAt().isBefore(LocalDateTime.now())) {
            removeSession(token);
            return null;
        }
        return authenticatedUser;
    }

    @Override
    public void removeSession(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        AuthenticatedUser removedUser = tokenStore.remove(token);
        if (removedUser != null && removedUser.getUserId() != null) {
            userTokenStore.remove(removedUser.getUserId(), token);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
