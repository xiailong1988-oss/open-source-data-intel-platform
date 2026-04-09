package com.haidian.intel.platform.modules.region.service;

import com.haidian.intel.platform.modules.region.dto.RegionTreeQueryDTO;
import com.haidian.intel.platform.modules.region.vo.RegionDetailVO;
import com.haidian.intel.platform.modules.region.vo.RegionGeoVO;
import com.haidian.intel.platform.modules.region.vo.RegionTreeNodeVO;
import java.util.List;

/**
 * 地区基础服务。
 */
public interface RegionService {

    /**
     * 获取地区树。
     *
     * @param queryDTO 查询参数
     * @return 排序后的地区树
     */
    List<RegionTreeNodeVO> getRegionTree(RegionTreeQueryDTO queryDTO);

    /**
     * 获取地区详情。
     *
     * @param regionId 地区 ID
     * @return 地区详情
     */
    RegionDetailVO getRegionDetail(Long regionId);

    /**
     * 获取地区边界信息。
     *
     * @param regionId 地区 ID
     * @return 地区边界对象
     */
    RegionGeoVO getRegionGeo(Long regionId);
}
