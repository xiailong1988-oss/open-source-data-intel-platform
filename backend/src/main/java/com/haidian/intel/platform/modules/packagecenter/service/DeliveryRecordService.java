package com.haidian.intel.platform.modules.packagecenter.service;

import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordCreateDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordPageQueryDTO;
import com.haidian.intel.platform.modules.packagecenter.vo.DeliveryRecordDetailVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DeliveryRecordPageVO;

/**
 * Service for delivery record management.
 */
public interface DeliveryRecordService {

    /**
     * Page delivery records for management.
     *
     * @param queryDTO query parameters
     * @return paged delivery record list
     */
    PageResponse<DeliveryRecordPageVO> pageRecords(DeliveryRecordPageQueryDTO queryDTO);

    /**
     * Get delivery record detail by ID.
     *
     * @param recordId delivery record ID
     * @return delivery record detail
     */
    DeliveryRecordDetailVO getRecordDetail(Long recordId);

    /**
     * Create a delivery record.
     *
     * @param createDTO create data
     * @return created delivery record detail
     */
    DeliveryRecordDetailVO createRecord(DeliveryRecordCreateDTO createDTO);
}
