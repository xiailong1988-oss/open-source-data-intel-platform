package com.haidian.intel.platform.modules.source.service;

import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.modules.source.dto.CollectionSourceCreateDTO;
import com.haidian.intel.platform.modules.source.dto.CollectionSourcePageQueryDTO;
import com.haidian.intel.platform.modules.source.dto.CollectionSourceUpdateDTO;
import com.haidian.intel.platform.modules.source.vo.CollectionSourceDetailVO;
import com.haidian.intel.platform.modules.source.vo.CollectionSourcePageVO;

/**
 * Service for collection source management.
 */
public interface CollectionSourceService {

    /**
     * Page sources for admin management.
     *
     * @param queryDTO query parameters
     * @return paged source list
     */
    PageResponse<CollectionSourcePageVO> pageSources(CollectionSourcePageQueryDTO queryDTO);

    /**
     * Get source detail.
     *
     * @param sourceId source ID
     * @return source detail
     */
    CollectionSourceDetailVO getSourceDetail(Long sourceId);

    /**
     * Create a source.
     *
     * @param createDTO create data
     * @return created source detail
     */
    CollectionSourceDetailVO createSource(CollectionSourceCreateDTO createDTO);

    /**
     * Update a source.
     *
     * @param sourceId source ID
     * @param updateDTO update data
     * @return updated source detail
     */
    CollectionSourceDetailVO updateSource(Long sourceId, CollectionSourceUpdateDTO updateDTO);

    /**
     * Delete a source.
     *
     * @param sourceId source ID
     */
    void deleteSource(Long sourceId);
}
