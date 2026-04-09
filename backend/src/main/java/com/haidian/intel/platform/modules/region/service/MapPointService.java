package com.haidian.intel.platform.modules.region.service;

import com.haidian.intel.platform.modules.region.dto.MapPointQueryDTO;
import com.haidian.intel.platform.modules.region.vo.MapPointVO;
import java.util.List;

/**
 * 地图点位服务。
 */
public interface MapPointService {

    /**
     * 查询首页地图点位。
     *
     * @param queryDTO 查询参数
     * @return 点位列表
     */
    List<MapPointVO> listMapPoints(MapPointQueryDTO queryDTO);
}
