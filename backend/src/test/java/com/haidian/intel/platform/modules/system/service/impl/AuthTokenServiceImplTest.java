package com.haidian.intel.platform.modules.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.haidian.intel.platform.common.auth.AuthenticatedUser;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for in-memory auth token service.
 */
class AuthTokenServiceImplTest {

    private final AuthTokenServiceImpl authTokenService = new AuthTokenServiceImpl();

    @Test
    void shouldCreateAndResolveTokenSession() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserId(1L);
        authenticatedUser.setUsername("admin");
        authenticatedUser.setRealName("默认管理员");
        authenticatedUser.setRoleCodes(List.of("ADMIN"));
        authenticatedUser.setRoleNames(List.of("系统管理员"));

        AuthenticatedUser createdSession = authTokenService.createSession(authenticatedUser);
        AuthenticatedUser resolvedSession = authTokenService.getSession(createdSession.getToken());

        assertThat(createdSession.getToken()).isNotBlank();
        assertThat(createdSession.getExpiresAt()).isNotNull();
        assertThat(resolvedSession).isNotNull();
        assertThat(resolvedSession.getUsername()).isEqualTo("admin");
        assertThat(resolvedSession.getRoleCodes()).containsExactly("ADMIN");
    }

    @Test
    void shouldInvalidatePreviousTokenWhenSameUserLogsInAgain() {
        AuthenticatedUser firstLogin = new AuthenticatedUser();
        firstLogin.setUserId(1L);
        firstLogin.setUsername("admin");
        firstLogin.setRoleCodes(List.of("ADMIN"));
        firstLogin.setRoleNames(List.of("系统管理员"));

        AuthenticatedUser secondLogin = new AuthenticatedUser();
        secondLogin.setUserId(1L);
        secondLogin.setUsername("admin");
        secondLogin.setRoleCodes(List.of("ADMIN"));
        secondLogin.setRoleNames(List.of("系统管理员"));

        String firstToken = authTokenService.createSession(firstLogin).getToken();
        String secondToken = authTokenService.createSession(secondLogin).getToken();

        assertThat(firstToken).isNotEqualTo(secondToken);
        assertThat(authTokenService.getSession(firstToken)).isNull();
        assertThat(authTokenService.getSession(secondToken)).isNotNull();
    }
}
