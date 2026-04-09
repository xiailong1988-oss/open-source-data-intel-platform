package com.haidian.intel.platform.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haidian.intel.platform.modules.system.entity.SystemOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper for system operation log persistence.
 */
@Mapper
public interface SystemOperLogMapper extends BaseMapper<SystemOperLog> {
}
