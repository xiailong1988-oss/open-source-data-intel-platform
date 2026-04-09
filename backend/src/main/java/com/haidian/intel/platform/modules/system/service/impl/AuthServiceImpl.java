package com.haidian.intel.platform.modules.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.haidian.intel.platform.common.auth.AuthenticatedUser;
import com.haidian.intel.platform.common.auth.UserContextHolder;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.common.util.PasswordUtils;
import com.haidian.intel.platform.modules.system.dto.LoginDTO;
import com.haidian.intel.platform.modules.system.entity.SystemRole;
import com.haidian.intel.platform.modules.system.entity.SystemUser;
import com.haidian.intel.platform.modules.system.entity.SystemUserRole;
import com.haidian.intel.platform.modules.system.mapper.SystemRoleMapper;
import com.haidian.intel.platform.modules.system.mapper.SystemUserMapper;
import com.haidian.intel.platform.modules.system.mapper.SystemUserRoleMapper;
import com.haidian.intel.platform.modules.system.service.AuthService;
import com.haidian.intel.platform.modules.system.service.AuthTokenService;
import com.haidian.intel.platform.modules.system.vo.CurrentUserVO;
import com.haidian.intel.platform.modules.system.vo.LoginVO;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service implementation for login, logout, and current user access.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final SystemUserMapper systemUserMapper;
    private final SystemRoleMapper systemRoleMapper;
    private final SystemUserRoleMapper systemUserRoleMapper;
    private final AuthTokenService authTokenService;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SystemUser user = systemUserMapper.selectOne(
                Wrappers.<SystemUser>lambdaQuery()
                        .eq(SystemUser::getUsername, loginDTO.getUsername())
                        .last("LIMIT 1")
        );
        if (user == null || !Objects.equals(user.getStatus(), 1) || !PasswordUtils.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Username or password is incorrect");
        }

        RoleSnapshot roleSnapshot = loadRoleSnapshot(user.getId());
        AuthenticatedUser authenticatedUser = buildAuthenticatedUser(user, roleSnapshot);
        authenticatedUser = authTokenService.createSession(authenticatedUser);

        SystemUser updater = new SystemUser();
        updater.setId(user.getId());
        updater.setLastLoginTime(LocalDateTime.now());
        systemUserMapper.updateById(updater);

        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(authenticatedUser.getToken());
        loginVO.setTokenType(TOKEN_TYPE);
        loginVO.setExpiresAt(authenticatedUser.getExpiresAt());
        loginVO.setCurrentUser(toCurrentUserVO(authenticatedUser));
        return loginVO;
    }

    @Override
    public void logout(String token) {
        authTokenService.removeSession(token);
    }

    @Override
    public CurrentUserVO getCurrentUser() {
        AuthenticatedUser authenticatedUser = UserContextHolder.getUser();
        if (authenticatedUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Please login first");
        }
        return toCurrentUserVO(authenticatedUser);
    }

    @Override
    public AuthenticatedUser resolveToken(String token) {
        return authTokenService.getSession(token);
    }

    private AuthenticatedUser buildAuthenticatedUser(SystemUser user, RoleSnapshot roleSnapshot) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserId(user.getId());
        authenticatedUser.setUsername(user.getUsername());
        authenticatedUser.setRealName(user.getRealName());
        authenticatedUser.setPhone(user.getPhone());
        authenticatedUser.setEmail(user.getEmail());
        authenticatedUser.setRoleCodes(roleSnapshot.roleCodes());
        authenticatedUser.setRoleNames(roleSnapshot.roleNames());
        return authenticatedUser;
    }

    private CurrentUserVO toCurrentUserVO(AuthenticatedUser authenticatedUser) {
        CurrentUserVO currentUserVO = new CurrentUserVO();
        currentUserVO.setUserId(authenticatedUser.getUserId());
        currentUserVO.setUsername(authenticatedUser.getUsername());
        currentUserVO.setRealName(authenticatedUser.getRealName());
        currentUserVO.setPhone(authenticatedUser.getPhone());
        currentUserVO.setEmail(authenticatedUser.getEmail());
        currentUserVO.setRoleCodes(authenticatedUser.getRoleCodes());
        currentUserVO.setRoleNames(authenticatedUser.getRoleNames());
        currentUserVO.setExpiresAt(authenticatedUser.getExpiresAt());
        return currentUserVO;
    }

    private RoleSnapshot loadRoleSnapshot(Long userId) {
        List<SystemUserRole> relations = systemUserRoleMapper.selectList(
                Wrappers.<SystemUserRole>lambdaQuery().eq(SystemUserRole::getUserId, userId)
        );
        if (relations == null || relations.isEmpty()) {
            return new RoleSnapshot(Collections.emptyList(), Collections.emptyList());
        }

        Set<Long> roleIds = relations.stream()
                .map(SystemUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return new RoleSnapshot(Collections.emptyList(), Collections.emptyList());
        }

        List<SystemRole> roles = systemRoleMapper.selectBatchIds(roleIds).stream()
                .filter(role -> Objects.equals(role.getStatus(), 1))
                .toList();
        List<String> roleCodes = roles.stream().map(SystemRole::getRoleCode).toList();
        List<String> roleNames = roles.stream().map(SystemRole::getRoleName).toList();
        return new RoleSnapshot(roleCodes, roleNames);
    }

    private record RoleSnapshot(List<String> roleCodes, List<String> roleNames) {
    }
}
