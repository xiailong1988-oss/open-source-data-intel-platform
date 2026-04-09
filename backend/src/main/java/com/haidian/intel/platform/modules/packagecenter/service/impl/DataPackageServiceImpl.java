package com.haidian.intel.platform.modules.packagecenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackageCreateDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackageFileSaveDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackagePageQueryDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackagePublishDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackageSaveDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackageUpdateDTO;
import com.haidian.intel.platform.modules.packagecenter.entity.DataPackage;
import com.haidian.intel.platform.modules.packagecenter.entity.DataPackageFile;
import com.haidian.intel.platform.modules.packagecenter.mapper.DataPackageFileMapper;
import com.haidian.intel.platform.modules.packagecenter.mapper.DataPackageMapper;
import com.haidian.intel.platform.modules.packagecenter.service.DataPackageService;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackageDetailVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackageFileVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackagePageVO;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Service implementation for data package management.
 */
@Service
@RequiredArgsConstructor
public class DataPackageServiceImpl implements DataPackageService {

    public static final String PACKAGE_STATUS_DRAFT = "draft";
    public static final String PACKAGE_STATUS_PUBLISHED = "published";
    private static final String DEFAULT_PUBLISHER = "system";
    private static final String DEFAULT_STORAGE_TYPE = "LOCAL";

    private static final Map<String, String> PACKAGE_TYPE_LABELS = Map.of(
            "topic_service", "专题服务包",
            "monthly_brief", "月度简报包",
            "special_dataset", "专题数据包",
            "delivery_archive", "交付归档包"
    );

    private static final Map<String, String> PACKAGE_STATUS_LABELS = Map.of(
            PACKAGE_STATUS_DRAFT, "草稿",
            PACKAGE_STATUS_PUBLISHED, "已发布"
    );

    private final DataPackageMapper dataPackageMapper;
    private final DataPackageFileMapper dataPackageFileMapper;

    @Override
    public PageResponse<DataPackagePageVO> pagePackages(DataPackagePageQueryDTO queryDTO) {
        DataPackagePageQueryDTO actualQuery = queryDTO == null ? new DataPackagePageQueryDTO() : queryDTO;
        long pageNum = normalizePageValue(actualQuery.getPageNum(), 1L);
        long pageSize = normalizePageValue(actualQuery.getPageSize(), 10L);

        Page<DataPackage> page = dataPackageMapper.selectPage(
                new Page<>(pageNum, pageSize),
                buildPageQuery(actualQuery)
        );
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return PageResponse.empty(pageNum, pageSize);
        }

        return new PageResponse<>(
                page.getRecords().stream().map(this::toPageVO).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    @Override
    public DataPackageDetailVO getPackageDetail(Long packageId) {
        return toDetailVO(getPackageOrThrow(packageId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataPackageDetailVO createPackage(DataPackageCreateDTO createDTO) {
        validatePackagePayload(createDTO);
        assertUniquePackage(createDTO.getPackageName(), createDTO.getStatMonth(), null);

        DataPackage dataPackage = new DataPackage();
        applySaveDTO(createDTO, dataPackage);
        dataPackage.setFileCount(0);
        dataPackage.setTotalSize(0L);
        dataPackage.setStatus(PACKAGE_STATUS_DRAFT);
        dataPackage.setPublishVersion(0);
        dataPackage.setPublishTime(null);
        dataPackage.setPublishedByName("");
        dataPackage.setPublishNote("");
        dataPackageMapper.insert(dataPackage);
        if (dataPackage.getId() == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Data package insert did not return an ID");
        }

        replacePackageFiles(dataPackage.getId(), createDTO.getFiles());
        refreshPackageSummary(dataPackage.getId());
        return getPackageDetail(dataPackage.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataPackageDetailVO updatePackage(Long packageId, DataPackageUpdateDTO updateDTO) {
        DataPackage existing = getPackageOrThrow(packageId);
        validatePackagePayload(updateDTO);
        assertUniquePackage(updateDTO.getPackageName(), updateDTO.getStatMonth(), packageId);

        applySaveDTO(updateDTO, existing);
        existing.setId(packageId);
        dataPackageMapper.updateById(existing);
        replacePackageFiles(packageId, updateDTO.getFiles());
        refreshPackageSummary(packageId);
        return getPackageDetail(packageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataPackageDetailVO publishPackage(Long packageId, DataPackagePublishDTO publishDTO) {
        DataPackage dataPackage = getPackageOrThrow(packageId);
        List<DataPackageFile> packageFiles = listPackageFileEntities(packageId);
        if (packageFiles.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Please maintain package files before publishing");
        }

        dataPackage.setStatus(PACKAGE_STATUS_PUBLISHED);
        dataPackage.setPublishVersion(defaultInteger(dataPackage.getPublishVersion()) + 1);
        dataPackage.setPublishTime(resolvePublishTime(publishDTO));
        dataPackage.setPublishedByName(resolvePublisherName(publishDTO));
        dataPackage.setPublishNote(defaultString(publishDTO == null ? null : publishDTO.getPublishNote()));
        dataPackageMapper.updateById(dataPackage);
        return getPackageDetail(packageId);
    }

    @Override
    public List<DataPackageFileVO> listPackageFiles(Long packageId) {
        getPackageOrThrow(packageId);
        return listPackageFileEntities(packageId).stream().map(this::toFileVO).toList();
    }

    private LambdaQueryWrapper<DataPackage> buildPageQuery(DataPackagePageQueryDTO queryDTO) {
        String keyword = StringUtils.trimWhitespace(queryDTO.getKeyword());
        LambdaQueryWrapper<DataPackage> queryWrapper = Wrappers.<DataPackage>lambdaQuery();
        queryWrapper.eq(StringUtils.hasText(queryDTO.getPackageType()), DataPackage::getPackageType, queryDTO.getPackageType());
        queryWrapper.eq(StringUtils.hasText(queryDTO.getStatMonth()), DataPackage::getStatMonth, queryDTO.getStatMonth());
        queryWrapper.eq(StringUtils.hasText(queryDTO.getStatus()), DataPackage::getStatus, queryDTO.getStatus());
        queryWrapper.and(
                StringUtils.hasText(keyword),
                wrapper -> wrapper.like(DataPackage::getPackageName, keyword)
                        .or()
                        .like(DataPackage::getRegionScope, keyword)
        );
        queryWrapper.orderByDesc(DataPackage::getUpdatedTime, DataPackage::getId);
        return queryWrapper;
    }

    private void validatePackagePayload(DataPackageSaveDTO saveDTO) {
        if (saveDTO == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Package payload must not be null");
        }
        validatePackageFiles(saveDTO.getFiles());
    }

    private void validatePackageFiles(List<DataPackageFileSaveDTO> files) {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        Set<String> uniqueKeys = new HashSet<>();
        for (DataPackageFileSaveDTO fileDTO : files) {
            String uniqueKey = fileDTO.getFileName() + "|" + fileDTO.getFilePath();
            if (!uniqueKeys.add(uniqueKey)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "Duplicate file name and path are not allowed");
            }
        }
    }

    private void assertUniquePackage(String packageName, String statMonth, Long excludeId) {
        DataPackage existing = dataPackageMapper.selectOne(
                Wrappers.<DataPackage>lambdaQuery()
                        .eq(DataPackage::getPackageName, packageName)
                        .eq(DataPackage::getStatMonth, statMonth)
                        .ne(excludeId != null, DataPackage::getId, excludeId)
                        .last("LIMIT 1")
        );
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "A data package with the same name and month already exists");
        }
    }

    private void applySaveDTO(DataPackageSaveDTO saveDTO, DataPackage dataPackage) {
        dataPackage.setPackageName(saveDTO.getPackageName());
        dataPackage.setPackageType(saveDTO.getPackageType());
        dataPackage.setStatMonth(saveDTO.getStatMonth());
        dataPackage.setRegionScope(saveDTO.getRegionScope());
        dataPackage.setDescription(defaultString(saveDTO.getDescription()));
        dataPackage.setRemark(defaultString(saveDTO.getRemark()));
    }

    private void replacePackageFiles(Long packageId, List<DataPackageFileSaveDTO> files) {
        dataPackageFileMapper.delete(
                Wrappers.<DataPackageFile>lambdaQuery().eq(DataPackageFile::getPackageId, packageId)
        );
        if (CollectionUtils.isEmpty(files)) {
            return;
        }

        for (DataPackageFileSaveDTO fileDTO : files) {
            DataPackageFile file = new DataPackageFile();
            file.setPackageId(packageId);
            file.setFileName(fileDTO.getFileName());
            file.setFilePath(fileDTO.getFilePath());
            file.setFileSize(defaultLong(fileDTO.getFileSize()));
            file.setFileType(defaultString(fileDTO.getFileType()));
            file.setChecksum(defaultString(fileDTO.getChecksum()));
            file.setStorageType(resolveStorageType(fileDTO.getStorageType()));
            file.setStorageBucket(defaultString(fileDTO.getStorageBucket()));
            file.setStorageObjectKey(defaultString(fileDTO.getStorageObjectKey()));
            file.setDownloadUrl(defaultString(fileDTO.getDownloadUrl()));
            file.setSortOrder(defaultInteger(fileDTO.getSortOrder()));
            file.setRemark(defaultString(fileDTO.getRemark()));
            dataPackageFileMapper.insert(file);
        }
    }

    private void refreshPackageSummary(Long packageId) {
        List<DataPackageFile> files = listPackageFileEntities(packageId);
        DataPackage updater = new DataPackage();
        updater.setId(packageId);
        updater.setFileCount(files.size());
        updater.setTotalSize(files.stream().map(DataPackageFile::getFileSize).mapToLong(this::defaultLong).sum());
        dataPackageMapper.updateById(updater);
    }

    private List<DataPackageFile> listPackageFileEntities(Long packageId) {
        List<DataPackageFile> files = dataPackageFileMapper.selectList(
                Wrappers.<DataPackageFile>lambdaQuery()
                        .eq(DataPackageFile::getPackageId, packageId)
                        .orderByAsc(DataPackageFile::getSortOrder, DataPackageFile::getId)
        );
        return files == null ? Collections.emptyList() : files;
    }

    private DataPackagePageVO toPageVO(DataPackage dataPackage) {
        DataPackagePageVO pageVO = new DataPackagePageVO();
        pageVO.setId(dataPackage.getId());
        pageVO.setPackageName(dataPackage.getPackageName());
        pageVO.setPackageType(dataPackage.getPackageType());
        pageVO.setPackageTypeLabel(resolveLabel(PACKAGE_TYPE_LABELS, dataPackage.getPackageType()));
        pageVO.setStatMonth(dataPackage.getStatMonth());
        pageVO.setRegionScope(dataPackage.getRegionScope());
        pageVO.setFileCount(defaultInteger(dataPackage.getFileCount()));
        pageVO.setTotalSize(defaultLong(dataPackage.getTotalSize()));
        pageVO.setStatus(dataPackage.getStatus());
        pageVO.setStatusLabel(resolveLabel(PACKAGE_STATUS_LABELS, dataPackage.getStatus()));
        pageVO.setPublishVersion(defaultInteger(dataPackage.getPublishVersion()));
        pageVO.setPublishTime(dataPackage.getPublishTime());
        pageVO.setPublishedByName(dataPackage.getPublishedByName());
        pageVO.setDescription(dataPackage.getDescription());
        pageVO.setCreatedTime(dataPackage.getCreatedTime());
        return pageVO;
    }

    private DataPackageDetailVO toDetailVO(DataPackage dataPackage) {
        DataPackageDetailVO detailVO = new DataPackageDetailVO();
        detailVO.setId(dataPackage.getId());
        detailVO.setPackageName(dataPackage.getPackageName());
        detailVO.setPackageType(dataPackage.getPackageType());
        detailVO.setPackageTypeLabel(resolveLabel(PACKAGE_TYPE_LABELS, dataPackage.getPackageType()));
        detailVO.setStatMonth(dataPackage.getStatMonth());
        detailVO.setRegionScope(dataPackage.getRegionScope());
        detailVO.setFileCount(defaultInteger(dataPackage.getFileCount()));
        detailVO.setTotalSize(defaultLong(dataPackage.getTotalSize()));
        detailVO.setStatus(dataPackage.getStatus());
        detailVO.setStatusLabel(resolveLabel(PACKAGE_STATUS_LABELS, dataPackage.getStatus()));
        detailVO.setPublishVersion(defaultInteger(dataPackage.getPublishVersion()));
        detailVO.setPublishTime(dataPackage.getPublishTime());
        detailVO.setPublishedByName(dataPackage.getPublishedByName());
        detailVO.setPublishNote(dataPackage.getPublishNote());
        detailVO.setDescription(dataPackage.getDescription());
        detailVO.setRemark(dataPackage.getRemark());
        detailVO.setCreatedTime(dataPackage.getCreatedTime());
        detailVO.setUpdatedTime(dataPackage.getUpdatedTime());
        return detailVO;
    }

    private DataPackageFileVO toFileVO(DataPackageFile file) {
        DataPackageFileVO fileVO = new DataPackageFileVO();
        fileVO.setId(file.getId());
        fileVO.setPackageId(file.getPackageId());
        fileVO.setFileName(file.getFileName());
        fileVO.setFilePath(file.getFilePath());
        fileVO.setFileSize(defaultLong(file.getFileSize()));
        fileVO.setFileType(file.getFileType());
        fileVO.setChecksum(file.getChecksum());
        fileVO.setStorageType(file.getStorageType());
        fileVO.setStorageBucket(file.getStorageBucket());
        fileVO.setStorageObjectKey(file.getStorageObjectKey());
        fileVO.setDownloadUrl(file.getDownloadUrl());
        fileVO.setSortOrder(defaultInteger(file.getSortOrder()));
        fileVO.setRemark(file.getRemark());
        fileVO.setCreatedTime(file.getCreatedTime());
        fileVO.setUpdatedTime(file.getUpdatedTime());
        return fileVO;
    }

    private DataPackage getPackageOrThrow(Long packageId) {
        DataPackage dataPackage = dataPackageMapper.selectById(packageId);
        if (dataPackage == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Data package does not exist");
        }
        return dataPackage;
    }

    private String resolveLabel(Map<String, String> labelMap, String code) {
        return labelMap.getOrDefault(code, code);
    }

    private String resolveStorageType(String storageType) {
        return StringUtils.hasText(storageType) ? storageType : DEFAULT_STORAGE_TYPE;
    }

    private LocalDateTime resolvePublishTime(DataPackagePublishDTO publishDTO) {
        if (publishDTO != null && publishDTO.getPublishTime() != null) {
            return publishDTO.getPublishTime();
        }
        return LocalDateTime.now();
    }

    private String resolvePublisherName(DataPackagePublishDTO publishDTO) {
        if (publishDTO != null && StringUtils.hasText(publishDTO.getPublishedByName())) {
            return publishDTO.getPublishedByName();
        }
        return DEFAULT_PUBLISHER;
    }

    private long normalizePageValue(Long value, long defaultValue) {
        return value == null || value < 1 ? defaultValue : value;
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
