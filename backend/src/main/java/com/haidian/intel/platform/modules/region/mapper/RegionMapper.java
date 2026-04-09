package com.haidian.intel.platform.modules.region.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haidian.intel.platform.modules.region.entity.Region;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地区主表 Mapper。
 */
@Mapper
public interface RegionMapper extends BaseMapper<Region> {
}
