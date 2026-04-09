package com.haidian.intel.platform.modules.intel.service;

import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.modules.intel.dto.IntelCreateDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelPageQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelStreamQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelTickerQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelUpdateDTO;
import com.haidian.intel.platform.modules.intel.vo.IntelDetailVO;
import com.haidian.intel.platform.modules.intel.vo.IntelPageVO;
import com.haidian.intel.platform.modules.intel.vo.IntelStreamVO;
import com.haidian.intel.platform.modules.intel.vo.IntelTickerVO;
import java.util.List;

/**
 * 情报核心服务。
 */
public interface IntelService {

    /**
     * 分页查询情报管理列表。
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResponse<IntelPageVO> pageIntel(IntelPageQueryDTO queryDTO);

    /**
     * 获取首页快讯条数据。
     *
     * @param queryDTO 查询参数
     * @return 快讯条列表
     */
    List<IntelTickerVO> listIntelTicker(IntelTickerQueryDTO queryDTO);

    /**
     * 获取首页主情报流分页数据。
     *
     * @param queryDTO 查询参数
     * @return 主情报流分页结果
     */
    PageResponse<IntelStreamVO> pageIntelStream(IntelStreamQueryDTO queryDTO);

    /**
     * 获取情报详情。
     *
     * @param intelId 情报ID
     * @return 情报详情
     */
    IntelDetailVO getIntelDetail(Long intelId);

    /**
     * 新增情报。
     *
     * @param createDTO 新增参数
     * @return 新增后的情报详情
     */
    IntelDetailVO createIntel(IntelCreateDTO createDTO);

    /**
     * 修改情报。
     *
     * @param intelId 情报ID
     * @param updateDTO 修改参数
     * @return 修改后的情报详情
     */
    IntelDetailVO updateIntel(Long intelId, IntelUpdateDTO updateDTO);

    /**
     * 删除情报。
     *
     * @param intelId 情报ID
     */
    void deleteIntel(Long intelId);
}
