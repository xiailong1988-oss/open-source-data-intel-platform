package com.haidian.intel.platform.modules.dashboard.service;

import com.haidian.intel.platform.modules.dashboard.dto.DashboardFocusTargetQueryDTO;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardHotspotQueryDTO;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardStatsQueryDTO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardFocusTargetVO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardHotspotVO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardOverviewVO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardRegionSummaryVO;
import java.util.List;

/**
 * 首页概览服务。
 */
public interface DashboardService {

    /**
     * 获取首页顶部概览统计。
     *
     * @param queryDTO 查询参数
     * @return 概览统计
     */
    DashboardOverviewVO getOverview(DashboardStatsQueryDTO queryDTO);

    /**
     * 获取地区态势摘要。
     *
     * @param queryDTO 查询参数
     * @return 地区态势摘要
     */
    DashboardRegionSummaryVO getRegionSummary(DashboardStatsQueryDTO queryDTO);

    /**
     * 获取首页热点专题列表。
     *
     * @param queryDTO 查询参数
     * @return 热点专题列表
     */
    List<DashboardHotspotVO> listHotspots(DashboardHotspotQueryDTO queryDTO);

    /**
     * 获取首页重点关注对象列表。
     *
     * @param queryDTO 查询参数
     * @return 重点关注对象列表
     */
    List<DashboardFocusTargetVO> listFocusTargets(DashboardFocusTargetQueryDTO queryDTO);
}
