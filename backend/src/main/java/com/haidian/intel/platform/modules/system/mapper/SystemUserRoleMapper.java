package com.haidian.intel.platform.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haidian.intel.platform.modules.system.entity.SystemUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper for system user-role relation persistence.
 */
@Mapper
public interface SystemUserRoleMapper extends BaseMapper<SystemUserRole> {
}
