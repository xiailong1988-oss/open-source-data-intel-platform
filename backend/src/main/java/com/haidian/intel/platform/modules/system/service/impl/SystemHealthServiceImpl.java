package com.haidian.intel.platform.modules.system.service.impl;

import com.haidian.intel.platform.modules.system.service.SystemHealthService;
import com.haidian.intel.platform.modules.system.vo.HealthStatusVO;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * 系统健康检查服务实现。
 */
@Service
public class SystemHealthServiceImpl implements SystemHealthService {

    private final Environment environment;

    public SystemHealthServiceImpl(Environment environment) {
        this.environment = environment;
    }

    @Override
    public HealthStatusVO getHealthStatus() {
        String profile = Arrays.stream(environment.getActiveProfiles()).findFirst().orElse("default");
        String applicationName = environment.getProperty("spring.application.name", "haidian-intel-platform-backend");
        return new HealthStatusVO(applicationName, "UP", profile, LocalDateTime.now());
    }
}
