package com.haidian.intel.platform.modules.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.haidian.intel.platform.common.auth.AuthenticatedUser;
import com.haidian.intel.platform.common.auth.UserContextHolder;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.common.util.PasswordUtils;
import com.haidian.intel.platform.modules.system.dto.LoginDTO;
import com.haidian.intel.platform.modules.system.entity.SystemRole;
import com.haidian.intel.platform.modules.system.entity.SystemUser;
import com.haidian.intel.platform.modules.system.entity.SystemUserRole;
import com.haidian.intel.platform.modules.system.mapper.SystemRoleMapper;
import com.haidian.intel.platform.modules.system.mapper.SystemUserMapper;
import com.haidian.intel.platform.modules.system.mapper.SystemUserRoleMapper;
import com.haidian.intel.platform.modules.system.service.AuthTokenService;
import com.haidian.intel.platform.modules.system.vo.CurrentUserVO;
import com.haidian.intel.platform.modules.system.vo.LoginVO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for auth service.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private SystemUserMapper systemUserMapper;

    @Mock
    private SystemRoleMapper systemRoleMapper;

    @Mock
    private SystemUserRoleMapper systemUserRoleMapper;

    @Mock
    private AuthTokenService authTokenService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(systemUserMapper, systemRoleMapper, systemUserRoleMapper, authTokenService);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void shouldLoginAndReturnCurrentUserSnapshot() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("Admin@123456");

        when(systemUserMapper.selectOne(any())).thenReturn(buildAdminUser());
        when(systemUserRoleMapper.selectList(any())).thenReturn(List.of(buildUserRole()));
        when(systemRoleMapper.selectBatchIds(any())).thenReturn(List.of(buildAdminRole()));
        when(authTokenService.createSession(any(AuthenticatedUser.class))).thenAnswer(invocation -> {
            AuthenticatedUser user = invocation.getArgument(0);
            user.setToken("token-123");
            user.setExpiresAt(LocalDateTime.of(2026, 4, 8, 23, 0));
            return user;
        });

        LoginVO loginVO = authService.login(loginDTO);

        assertThat(loginVO.getAccessToken()).isEqualTo("token-123");
        assertThat(loginVO.getTokenType()).isEqualTo("Bearer");
        assertThat(loginVO.getCurrentUser().getUsername()).isEqualTo("admin");
        assertThat(loginVO.getCurrentUser().getRoleCodes()).containsExactly("ADMIN");
        assertThat(loginVO.getCurrentUser().getRoleNames()).containsExactly("系统管理员");
    }

    @Test
    void shouldRejectLoginWhenPasswordIsIncorrect() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrong-password");

        when(systemUserMapper.selectOne(any())).thenReturn(buildAdminUser());

        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Username or password is incorrect");
    }

    @Test
    void shouldReturnCurrentUserFromContext() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserId(1L);
        authenticatedUser.setUsername("admin");
        authenticatedUser.setRealName("默认管理员");
        authenticatedUser.setPhone("");
        authenticatedUser.setEmail("admin@local.test");
        authenticatedUser.setRoleCodes(List.of("ADMIN"));
        authenticatedUser.setRoleNames(List.of("系统管理员"));
        authenticatedUser.setExpiresAt(LocalDateTime.of(2026, 4, 8, 23, 0));
        UserContextHolder.setUser(authenticatedUser);

        CurrentUserVO currentUserVO = authService.getCurrentUser();

        assertThat(currentUserVO.getUserId()).isEqualTo(1L);
        assertThat(currentUserVO.getUsername()).isEqualTo("admin");
        assertThat(currentUserVO.getRoleCodes()).containsExactly("ADMIN");
    }

    private SystemUser buildAdminUser() {
        SystemUser user = new SystemUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(PasswordUtils.encode("Admin@123456"));
        user.setRealName("默认管理员");
        user.setEmail("admin@local.test");
        user.setStatus(1);
        return user;
    }

    private SystemUserRole buildUserRole() {
        SystemUserRole userRole = new SystemUserRole();
        userRole.setId(1L);
        userRole.setUserId(1L);
        userRole.setRoleId(1L);
        return userRole;
    }

    private SystemRole buildAdminRole() {
        SystemRole role = new SystemRole();
        role.setId(1L);
        role.setRoleCode("ADMIN");
        role.setRoleName("系统管理员");
        role.setStatus(1);
        return role;
    }
}
