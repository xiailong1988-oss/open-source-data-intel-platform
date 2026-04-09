package com.haidian.intel.platform.common.auth;

import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for lightweight login and role checks on protected endpoints.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_PREFIX = "Bearer ";
    private static final String LEGACY_TOKEN_HEADER = "X-Auth-Token";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserContextHolder.clear();
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AuthenticatedUser authenticatedUser = resolveAuthenticatedUser(request);
        if (authenticatedUser != null) {
            UserContextHolder.setUser(authenticatedUser);
        }

        RequireRole requireRole = findRequireRole(handlerMethod);
        boolean loginRequired = requireRole != null || isLoginRequired(handlerMethod);
        if (!loginRequired) {
            return true;
        }

        if (authenticatedUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Please login first");
        }
        if (requireRole == null) {
            return true;
        }
        if (Collections.disjoint(authenticatedUser.getRoleCodes(), Arrays.asList(requireRole.value()))) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Current user does not have permission");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }

    private AuthenticatedUser resolveAuthenticatedUser(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            return null;
        }
        return authService.resolveToken(token);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization != null && authorization.startsWith(AUTHORIZATION_PREFIX)) {
            return authorization.substring(AUTHORIZATION_PREFIX.length()).trim();
        }
        return request.getHeader(LEGACY_TOKEN_HEADER);
    }

    private boolean isLoginRequired(HandlerMethod handlerMethod) {
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), LoginRequired.class) != null
                || AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), LoginRequired.class) != null;
    }

    private RequireRole findRequireRole(HandlerMethod handlerMethod) {
        RequireRole methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequireRole.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequireRole.class);
    }
}
