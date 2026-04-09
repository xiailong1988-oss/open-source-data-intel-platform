package com.haidian.intel.platform.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haidian.intel.platform.modules.system.entity.SystemUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper for system user persistence.
 */
@Mapper
public interface SystemUserMapper extends BaseMapper<SystemUser> {
}
