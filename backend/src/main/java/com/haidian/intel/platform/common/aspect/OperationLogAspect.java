package com.haidian.intel.platform.common.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haidian.intel.platform.common.auth.AuthenticatedUser;
import com.haidian.intel.platform.common.auth.RequireRole;
import com.haidian.intel.platform.common.auth.UserContextHolder;
import com.haidian.intel.platform.modules.system.entity.SystemOperLog;
import com.haidian.intel.platform.modules.system.mapper.SystemOperLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 * Aspect for automatic operation log recording.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int MAX_LOG_TEXT_LENGTH = 4000;
    private static final String MASKED_VALUE = "\"***\"";

    private final ObjectMapper objectMapper;
    private final SystemOperLogMapper systemOperLogMapper;

    @Around(
            "@within(com.haidian.intel.platform.common.auth.RequireRole)"
                    + " || @annotation(com.haidian.intel.platform.common.auth.RequireRole)"
                    + " || execution(* com.haidian.intel.platform.modules.system.controller.AuthController.*(..))"
    )
    public Object aroundProtectedApi(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        SystemOperLog systemOperLog = new SystemOperLog();
        systemOperLog.setModuleName(resolveModuleName(targetClass));
        systemOperLog.setActionName(resolveActionName(methodSignature));
        systemOperLog.setRequestParam(truncate(serializeArguments(joinPoint.getArgs())));
        systemOperLog.setOperateTime(LocalDateTime.now());
        fillRequestInfo(systemOperLog, requestAttributes);
        fillOperatorInfo(systemOperLog, UserContextHolder.getUser());

        try {
            Object result = joinPoint.proceed();
            systemOperLog.setSuccessFlag(1);
            systemOperLog.setResponseData(truncate(serializeSafely(result)));
            saveLog(systemOperLog);
            return result;
        } catch (Throwable throwable) {
            systemOperLog.setSuccessFlag(0);
            systemOperLog.setResponseData(truncate(throwable.getClass().getSimpleName() + ":" + throwable.getMessage()));
            saveLog(systemOperLog);
            throw throwable;
        }
    }

    private void fillRequestInfo(SystemOperLog systemOperLog, RequestAttributes requestAttributes) {
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return;
        }
        systemOperLog.setRequestUri(servletRequestAttributes.getRequest().getRequestURI());
        systemOperLog.setRequestMethod(servletRequestAttributes.getRequest().getMethod());
    }

    private void fillOperatorInfo(SystemOperLog systemOperLog, AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            systemOperLog.setOperatorId(0L);
            systemOperLog.setOperatorName("");
            return;
        }
        systemOperLog.setOperatorId(authenticatedUser.getUserId());
        systemOperLog.setOperatorName(
                StringUtils.hasText(authenticatedUser.getRealName())
                        ? authenticatedUser.getRealName()
                        : authenticatedUser.getUsername()
        );
    }

    private String resolveModuleName(Class<?> targetClass) {
        Tag tag = targetClass.getAnnotation(Tag.class);
        return tag == null ? targetClass.getSimpleName() : tag.name();
    }

    private String resolveActionName(MethodSignature methodSignature) {
        Operation operation = methodSignature.getMethod().getAnnotation(Operation.class);
        return operation == null ? methodSignature.getMethod().getName() : operation.summary();
    }

    private String serializeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        List<Object> filteredArgs = new ArrayList<>();
        for (Object arg : args) {
            if (shouldSkipArgument(arg)) {
                continue;
            }
            filteredArgs.add(arg);
        }
        return serializeSafely(filteredArgs);
    }

    private boolean shouldSkipArgument(Object arg) {
        return arg == null
                || arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof BindingResult
                || arg instanceof MultipartFile;
    }

    private String serializeSafely(Object object) {
        String serializedValue;
        try {
            serializedValue = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            serializedValue = String.valueOf(object);
        }
        return sanitizeSensitiveContent(serializedValue);
    }

    private String sanitizeSensitiveContent(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value
                .replaceAll("(\"password\"\\s*:\\s*)\"[^\"]*\"", "$1" + MASKED_VALUE)
                .replaceAll("(\"accessToken\"\\s*:\\s*)\"[^\"]*\"", "$1" + MASKED_VALUE)
                .replaceAll("(\"token\"\\s*:\\s*)\"[^\"]*\"", "$1" + MASKED_VALUE)
                .replaceAll("(\"authorization\"\\s*:\\s*)\"[^\"]*\"", "$1" + MASKED_VALUE)
                .replaceAll("(\"Authorization\"\\s*:\\s*)\"[^\"]*\"", "$1" + MASKED_VALUE);
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_LOG_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LOG_TEXT_LENGTH);
    }

    private void saveLog(SystemOperLog systemOperLog) {
        try {
            systemOperLogMapper.insert(systemOperLog);
        } catch (Exception exception) {
            log.warn("Failed to persist operation log for uri={}", systemOperLog.getRequestUri(), exception);
        }
    }
}
