package com.haidian.intel.platform.modules.system.service;

import com.haidian.intel.platform.modules.system.vo.HealthStatusVO;

/**
 * 系统健康检查服务接口。
 */
public interface SystemHealthService {

    HealthStatusVO getHealthStatus();
}
