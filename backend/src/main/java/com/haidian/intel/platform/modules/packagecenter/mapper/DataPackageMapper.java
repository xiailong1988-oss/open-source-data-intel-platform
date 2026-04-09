package com.haidian.intel.platform.modules.packagecenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haidian.intel.platform.modules.packagecenter.entity.DataPackage;
import org.apache.ibatis.annotations.Mapper;

/**
 * Mapper for data package persistence.
 */
@Mapper
public interface DataPackageMapper extends BaseMapper<DataPackage> {
}
