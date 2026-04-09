package com.haidian.intel.platform.modules.packagecenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordCreateDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordPageQueryDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordSaveDTO;
import com.haidian.intel.platform.modules.packagecenter.entity.DataPackage;
import com.haidian.intel.platform.modules.packagecenter.entity.DeliveryRecord;
import com.haidian.intel.platform.modules.packagecenter.mapper.DataPackageMapper;
import com.haidian.intel.platform.modules.packagecenter.mapper.DeliveryRecordMapper;
import com.haidian.intel.platform.modules.packagecenter.service.DeliveryRecordService;
import com.haidian.intel.platform.modules.packagecenter.vo.DeliveryRecordDetailVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DeliveryRecordPageVO;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service implementation for delivery record management.
 */
@Service
@RequiredArgsConstructor
public class DeliveryRecordServiceImpl implements DeliveryRecordService {

    public static final String DELIVERY_TYPE_DATA_PACKAGE = "data_package";

    private static final Map<String, String> DELIVERY_TYPE_LABELS = Map.of(
            DELIVERY_TYPE_DATA_PACKAGE, "数据包"
    );

    private static final Map<String, String> DELIVERY_METHOD_LABELS = Map.of(
            "online_download", "在线下载",
            "email", "邮件发送",
            "disk_handover", "介质交付",
            "api_push", "接口推送",
            "usb_copy", "拷贝交付"
    );

    private final DeliveryRecordMapper deliveryRecordMapper;
    private final DataPackageMapper dataPackageMapper;

    @Override
    public PageResponse<DeliveryRecordPageVO> pageRecords(DeliveryRecordPageQueryDTO queryDTO) {
        DeliveryRecordPageQueryDTO actualQuery = queryDTO == null ? new DeliveryRecordPageQueryDTO() : queryDTO;
        long pageNum = normalizePageValue(actualQuery.getPageNum(), 1L);
        long pageSize = normalizePageValue(actualQuery.getPageSize(), 10L);

        Page<DeliveryRecord> page = deliveryRecordMapper.selectPage(
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
    public DeliveryRecordDetailVO getRecordDetail(Long recordId) {
        return toDetailVO(getRecordOrThrow(recordId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryRecordDetailVO createRecord(DeliveryRecordCreateDTO createDTO) {
        validatePayload(createDTO);
        DataPackage dataPackage = getPackageOrThrow(createDTO.getRelatedId());
        if (!DataPackageServiceImpl.PACKAGE_STATUS_PUBLISHED.equals(dataPackage.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Only published data packages can be delivered");
        }

        DeliveryRecord record = new DeliveryRecord();
        applySaveDTO(createDTO, dataPackage, record);
        deliveryRecordMapper.insert(record);
        if (record.getId() == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Delivery record insert did not return an ID");
        }
        return getRecordDetail(record.getId());
    }

    private LambdaQueryWrapper<DeliveryRecord> buildPageQuery(DeliveryRecordPageQueryDTO queryDTO) {
        String keyword = StringUtils.trimWhitespace(queryDTO.getKeyword());
        LambdaQueryWrapper<DeliveryRecord> queryWrapper = Wrappers.<DeliveryRecord>lambdaQuery();
        queryWrapper.eq(StringUtils.hasText(queryDTO.getDeliveryType()), DeliveryRecord::getDeliveryType, queryDTO.getDeliveryType());
        queryWrapper.eq(queryDTO.getRelatedId() != null, DeliveryRecord::getRelatedId, queryDTO.getRelatedId());
        queryWrapper.eq(StringUtils.hasText(queryDTO.getDeliveryMethod()), DeliveryRecord::getDeliveryMethod, queryDTO.getDeliveryMethod());
        queryWrapper.and(
                StringUtils.hasText(keyword),
                wrapper -> wrapper.like(DeliveryRecord::getRelatedName, keyword)
                        .or()
                        .like(DeliveryRecord::getReceiverName, keyword)
                        .or()
                        .like(DeliveryRecord::getReceiverOrg, keyword)
        );
        queryWrapper.orderByDesc(DeliveryRecord::getDeliveryTime, DeliveryRecord::getId);
        return queryWrapper;
    }

    private void validatePayload(DeliveryRecordSaveDTO saveDTO) {
        if (saveDTO == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Delivery payload must not be null");
        }
        if (!DELIVERY_TYPE_DATA_PACKAGE.equals(saveDTO.getDeliveryType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Only data_package deliveryType is supported in the current task");
        }
    }

    private void applySaveDTO(DeliveryRecordSaveDTO saveDTO, DataPackage dataPackage, DeliveryRecord record) {
        record.setDeliveryType(saveDTO.getDeliveryType());
        record.setRelatedId(saveDTO.getRelatedId());
        record.setRelatedName(dataPackage.getPackageName());
        record.setRelatedVersion(defaultInteger(dataPackage.getPublishVersion()));
        record.setReceiverName(saveDTO.getReceiverName());
        record.setReceiverOrg(defaultString(saveDTO.getReceiverOrg()));
        record.setContactPhone(defaultString(saveDTO.getContactPhone()));
        record.setDeliveryTime(saveDTO.getDeliveryTime());
        record.setDeliveryMethod(saveDTO.getDeliveryMethod());
        record.setDescription(defaultString(saveDTO.getDescription()));
        record.setRemark(defaultString(saveDTO.getRemark()));
    }

    private DeliveryRecordPageVO toPageVO(DeliveryRecord record) {
        DeliveryRecordPageVO pageVO = new DeliveryRecordPageVO();
        pageVO.setId(record.getId());
        pageVO.setDeliveryType(record.getDeliveryType());
        pageVO.setDeliveryTypeLabel(resolveLabel(DELIVERY_TYPE_LABELS, record.getDeliveryType()));
        pageVO.setRelatedId(record.getRelatedId());
        pageVO.setRelatedName(record.getRelatedName());
        pageVO.setRelatedVersion(defaultInteger(record.getRelatedVersion()));
        pageVO.setReceiverName(record.getReceiverName());
        pageVO.setReceiverOrg(record.getReceiverOrg());
        pageVO.setDeliveryTime(record.getDeliveryTime());
        pageVO.setDeliveryMethod(record.getDeliveryMethod());
        pageVO.setDeliveryMethodLabel(resolveLabel(DELIVERY_METHOD_LABELS, record.getDeliveryMethod()));
        pageVO.setDescription(record.getDescription());
        pageVO.setCreatedTime(record.getCreatedTime());
        return pageVO;
    }

    private DeliveryRecordDetailVO toDetailVO(DeliveryRecord record) {
        DeliveryRecordDetailVO detailVO = new DeliveryRecordDetailVO();
        detailVO.setId(record.getId());
        detailVO.setDeliveryType(record.getDeliveryType());
        detailVO.setDeliveryTypeLabel(resolveLabel(DELIVERY_TYPE_LABELS, record.getDeliveryType()));
        detailVO.setRelatedId(record.getRelatedId());
        detailVO.setRelatedName(record.getRelatedName());
        detailVO.setRelatedVersion(defaultInteger(record.getRelatedVersion()));
        detailVO.setReceiverName(record.getReceiverName());
        detailVO.setReceiverOrg(record.getReceiverOrg());
        detailVO.setContactPhone(record.getContactPhone());
        detailVO.setDeliveryTime(record.getDeliveryTime());
        detailVO.setDeliveryMethod(record.getDeliveryMethod());
        detailVO.setDeliveryMethodLabel(resolveLabel(DELIVERY_METHOD_LABELS, record.getDeliveryMethod()));
        detailVO.setDescription(record.getDescription());
        detailVO.setRemark(record.getRemark());
        detailVO.setCreatedTime(record.getCreatedTime());
        detailVO.setUpdatedTime(record.getUpdatedTime());
        return detailVO;
    }

    private DeliveryRecord getRecordOrThrow(Long recordId) {
        DeliveryRecord record = deliveryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Delivery record does not exist");
        }
        return record;
    }

    private DataPackage getPackageOrThrow(Long packageId) {
        DataPackage dataPackage = dataPackageMapper.selectById(packageId);
        if (dataPackage == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Related data package does not exist");
        }
        return dataPackage;
    }

    private String resolveLabel(Map<String, String> labelMap, String code) {
        return labelMap.getOrDefault(code, code);
    }

    private long normalizePageValue(Long value, long defaultValue) {
        return value == null || value < 1 ? defaultValue : value;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
