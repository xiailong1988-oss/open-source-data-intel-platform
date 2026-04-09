package com.haidian.intel.platform.modules.region.service;

import com.haidian.intel.platform.modules.region.dto.MapLayerQueryDTO;
import com.haidian.intel.platform.modules.region.vo.MapLayerVO;
import java.util.List;

/**
 * 地图图层基础服务。
 */
public interface MapLayerService {

    /**
     * 获取地图图层列表。
     *
     * @param queryDTO 查询参数
     * @return 排序后的图层列表
     */
    List<MapLayerVO> getMapLayers(MapLayerQueryDTO queryDTO);
}
