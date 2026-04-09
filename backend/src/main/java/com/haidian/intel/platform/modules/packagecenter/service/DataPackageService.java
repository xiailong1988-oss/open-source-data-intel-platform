package com.haidian.intel.platform.modules.packagecenter.service;

import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackageCreateDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackagePageQueryDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackagePublishDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackageUpdateDTO;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackageDetailVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackageFileVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackagePageVO;
import java.util.List;

/**
 * Service for data package management.
 */
public interface DataPackageService {

    /**
     * Page data packages for management.
     *
     * @param queryDTO query parameters
     * @return paged data package list
     */
    PageResponse<DataPackagePageVO> pagePackages(DataPackagePageQueryDTO queryDTO);

    /**
     * Get package detail by ID.
     *
     * @param packageId data package ID
     * @return package detail
     */
    DataPackageDetailVO getPackageDetail(Long packageId);

    /**
     * Create a data package.
     *
     * @param createDTO create data
     * @return created package detail
     */
    DataPackageDetailVO createPackage(DataPackageCreateDTO createDTO);

    /**
     * Update a data package.
     *
     * @param packageId data package ID
     * @param updateDTO update data
     * @return updated package detail
     */
    DataPackageDetailVO updatePackage(Long packageId, DataPackageUpdateDTO updateDTO);

    /**
     * Publish a data package.
     *
     * @param packageId data package ID
     * @param publishDTO publish data
     * @return published package detail
     */
    DataPackageDetailVO publishPackage(Long packageId, DataPackagePublishDTO publishDTO);

    /**
     * List package files.
     *
     * @param packageId data package ID
     * @return package file list
     */
    List<DataPackageFileVO> listPackageFiles(Long packageId);
}
