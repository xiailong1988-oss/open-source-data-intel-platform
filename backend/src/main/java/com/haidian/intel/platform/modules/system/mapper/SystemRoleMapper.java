package com.haidian.intel.platform.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haidian.intel.platform.modules.system.entity.SystemRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper for system role persistence.
 */
@Mapper
public interface SystemRoleMapper extends BaseMapper<SystemRole> {
}
