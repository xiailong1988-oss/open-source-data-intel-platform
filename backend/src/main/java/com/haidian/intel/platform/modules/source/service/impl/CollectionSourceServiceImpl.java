package com.haidian.intel.platform.modules.source.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.collect.entity.CollectionTask;
import com.haidian.intel.platform.modules.collect.mapper.CollectionTaskMapper;
import com.haidian.intel.platform.modules.source.dto.CollectionSourceCreateDTO;
import com.haidian.intel.platform.modules.source.dto.CollectionSourcePageQueryDTO;
import com.haidian.intel.platform.modules.source.dto.CollectionSourceSaveDTO;
import com.haidian.intel.platform.modules.source.dto.CollectionSourceUpdateDTO;
import com.haidian.intel.platform.modules.source.entity.CollectionSource;
import com.haidian.intel.platform.modules.source.mapper.CollectionSourceMapper;
import com.haidian.intel.platform.modules.source.service.CollectionSourceService;
import com.haidian.intel.platform.modules.source.vo.CollectionSourceDetailVO;
import com.haidian.intel.platform.modules.source.vo.CollectionSourcePageVO;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service implementation for collection source management.
 */
@Service
@RequiredArgsConstructor
public class CollectionSourceServiceImpl implements CollectionSourceService {

    private static final int DEFAULT_ENABLED = 1;
    private static final String SOURCE_STATUS_RUNNING = "running";
    private static final String SOURCE_STATUS_STOPPED = "stopped";
    private static final String SOURCE_STATUS_ABNORMAL = "abnormal";

    private static final Map<String, String> SOURCE_CATEGORY_LABELS = Map.of(
            "gov_system", "政务系统",
            "business_system", "业务系统",
            "public_data", "互联网公开数据",
            "iot_device", "物联网设备",
            "third_party", "第三方平台"
    );

    private static final Map<String, String> ACCESS_TYPE_LABELS = Map.of(
            "api", "API接入",
            "database_sync", "数据库同步",
            "file_import", "文件导入",
            "message_subscribe", "消息订阅"
    );

    private static final Map<String, String> SOURCE_STATUS_LABELS = Map.of(
            SOURCE_STATUS_RUNNING, "运行中",
            SOURCE_STATUS_STOPPED, "已停用",
            SOURCE_STATUS_ABNORMAL, "异常"
    );

    private final CollectionSourceMapper collectionSourceMapper;
    private final CollectionTaskMapper collectionTaskMapper;

    @Override
    public PageResponse<CollectionSourcePageVO> pageSources(CollectionSourcePageQueryDTO queryDTO) {
        CollectionSourcePageQueryDTO actualQuery = queryDTO == null ? new CollectionSourcePageQueryDTO() : queryDTO;
        long pageNum = normalizePageValue(actualQuery.getPageNum(), 1L);
        long pageSize = normalizePageValue(actualQuery.getPageSize(), 10L);

        Page<CollectionSource> page = collectionSourceMapper.selectPage(
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
    public CollectionSourceDetailVO getSourceDetail(Long sourceId) {
        return toDetailVO(getSourceOrThrow(sourceId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectionSourceDetailVO createSource(CollectionSourceCreateDTO createDTO) {
        validateSourcePayload(createDTO);
        assertUniqueSourceName(createDTO.getSourceName(), null);

        CollectionSource source = new CollectionSource();
        applySaveDTO(createDTO, source);
        collectionSourceMapper.insert(source);
        if (source.getId() == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "数据源新增后未返回主键");
        }
        return getSourceDetail(source.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectionSourceDetailVO updateSource(Long sourceId, CollectionSourceUpdateDTO updateDTO) {
        CollectionSource existing = getSourceOrThrow(sourceId);
        validateSourcePayload(updateDTO);
        assertUniqueSourceName(updateDTO.getSourceName(), sourceId);

        applySaveDTO(updateDTO, existing);
        existing.setId(sourceId);
        collectionSourceMapper.updateById(existing);
        return getSourceDetail(sourceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSource(Long sourceId) {
        getSourceOrThrow(sourceId);
        long relatedTaskCount = collectionTaskMapper.selectCount(
                Wrappers.<CollectionTask>lambdaQuery().eq(CollectionTask::getSourceId, sourceId)
        );
        if (relatedTaskCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先删除关联采集任务后再删除数据源");
        }
        collectionSourceMapper.deleteById(sourceId);
    }

    private LambdaQueryWrapper<CollectionSource> buildPageQuery(CollectionSourcePageQueryDTO queryDTO) {
        String keyword = StringUtils.trimWhitespace(queryDTO.getKeyword());
        LambdaQueryWrapper<CollectionSource> queryWrapper = Wrappers.<CollectionSource>lambdaQuery();
        queryWrapper.eq(
                StringUtils.hasText(queryDTO.getSourceCategory()),
                CollectionSource::getSourceCategory,
                queryDTO.getSourceCategory()
        );
        queryWrapper.eq(
                StringUtils.hasText(queryDTO.getAccessType()),
                CollectionSource::getAccessType,
                queryDTO.getAccessType()
        );
        queryWrapper.eq(queryDTO.getEnabled() != null, CollectionSource::getEnabled, queryDTO.getEnabled());
        queryWrapper.eq(StringUtils.hasText(queryDTO.getStatus()), CollectionSource::getStatus, queryDTO.getStatus());
        queryWrapper.and(
                StringUtils.hasText(keyword),
                wrapper -> wrapper.like(CollectionSource::getSourceName, keyword)
                        .or()
                        .like(CollectionSource::getOwnerName, keyword)
                        .or()
                        .like(CollectionSource::getSourceUrl, keyword)
        );
        queryWrapper.orderByDesc(CollectionSource::getUpdatedTime, CollectionSource::getId);
        return queryWrapper;
    }

    private void validateSourcePayload(CollectionSourceSaveDTO saveDTO) {
        assertSupportedValue(saveDTO.getSourceCategory(), SOURCE_CATEGORY_LABELS, "不支持的数据源分类");
        assertSupportedValue(saveDTO.getAccessType(), ACCESS_TYPE_LABELS, "不支持的接入方式");
        String normalizedStatus = normalizeSourceStatus(saveDTO.getStatus(), normalizeEnabled(saveDTO.getEnabled()));
        assertSupportedValue(normalizedStatus, SOURCE_STATUS_LABELS, "不支持的数据源状态");
    }

    private void assertUniqueSourceName(String sourceName, Long excludeId) {
        CollectionSource existing = collectionSourceMapper.selectOne(
                Wrappers.<CollectionSource>lambdaQuery()
                        .eq(CollectionSource::getSourceName, sourceName)
                        .ne(excludeId != null, CollectionSource::getId, excludeId)
                        .last("LIMIT 1")
        );
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "数据源名称已存在");
        }
    }

    private void applySaveDTO(CollectionSourceSaveDTO saveDTO, CollectionSource source) {
        int enabled = normalizeEnabled(saveDTO.getEnabled());
        source.setSourceName(saveDTO.getSourceName());
        source.setSourceCategory(saveDTO.getSourceCategory());
        source.setSourceUrl(saveDTO.getSourceUrl());
        source.setAccessType(saveDTO.getAccessType());
        source.setEnabled(enabled);
        source.setStatus(normalizeSourceStatus(saveDTO.getStatus(), enabled));
        source.setRegionScope(defaultString(saveDTO.getRegionScope()));
        source.setOwnerName(saveDTO.getOwnerName());
        source.setLatestCollectTime(saveDTO.getLatestCollectTime());
        source.setDescription(defaultString(saveDTO.getDescription()));
        source.setRemark(defaultString(saveDTO.getRemark()));
    }

    private CollectionSourcePageVO toPageVO(CollectionSource source) {
        CollectionSourcePageVO pageVO = new CollectionSourcePageVO();
        pageVO.setId(source.getId());
        pageVO.setSourceName(source.getSourceName());
        pageVO.setSourceCategory(source.getSourceCategory());
        pageVO.setSourceCategoryLabel(resolveLabel(SOURCE_CATEGORY_LABELS, source.getSourceCategory()));
        pageVO.setAccessType(source.getAccessType());
        pageVO.setAccessTypeLabel(resolveLabel(ACCESS_TYPE_LABELS, source.getAccessType()));
        pageVO.setEnabled(source.getEnabled());
        pageVO.setStatus(source.getStatus());
        pageVO.setStatusLabel(resolveLabel(SOURCE_STATUS_LABELS, source.getStatus()));
        pageVO.setOwnerName(source.getOwnerName());
        pageVO.setSourceUrl(source.getSourceUrl());
        pageVO.setLatestCollectTime(source.getLatestCollectTime());
        pageVO.setCreatedTime(source.getCreatedTime());
        pageVO.setDescription(source.getDescription());
        return pageVO;
    }

    private CollectionSourceDetailVO toDetailVO(CollectionSource source) {
        CollectionSourceDetailVO detailVO = new CollectionSourceDetailVO();
        detailVO.setId(source.getId());
        detailVO.setSourceName(source.getSourceName());
        detailVO.setSourceCategory(source.getSourceCategory());
        detailVO.setSourceCategoryLabel(resolveLabel(SOURCE_CATEGORY_LABELS, source.getSourceCategory()));
        detailVO.setAccessType(source.getAccessType());
        detailVO.setAccessTypeLabel(resolveLabel(ACCESS_TYPE_LABELS, source.getAccessType()));
        detailVO.setEnabled(source.getEnabled());
        detailVO.setStatus(source.getStatus());
        detailVO.setStatusLabel(resolveLabel(SOURCE_STATUS_LABELS, source.getStatus()));
        detailVO.setRegionScope(source.getRegionScope());
        detailVO.setOwnerName(source.getOwnerName());
        detailVO.setSourceUrl(source.getSourceUrl());
        detailVO.setLatestCollectTime(source.getLatestCollectTime());
        detailVO.setDescription(source.getDescription());
        detailVO.setRemark(source.getRemark());
        detailVO.setCreatedTime(source.getCreatedTime());
        detailVO.setUpdatedTime(source.getUpdatedTime());
        return detailVO;
    }

    private CollectionSource getSourceOrThrow(Long sourceId) {
        CollectionSource source = collectionSourceMapper.selectById(sourceId);
        if (source == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "数据源不存在");
        }
        return source;
    }

    private void assertSupportedValue(String code, Map<String, String> supportedValues, String message) {
        if (!supportedValues.containsKey(code)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, message);
        }
    }

    private String resolveLabel(Map<String, String> labelMap, String code) {
        return labelMap.getOrDefault(code, code);
    }

    private int normalizeEnabled(Integer enabled) {
        return enabled == null ? DEFAULT_ENABLED : (enabled == 0 ? 0 : 1);
    }

    private String normalizeSourceStatus(String status, int enabled) {
        if (enabled == 0) {
            return SOURCE_STATUS_STOPPED;
        }
        if (SOURCE_STATUS_ABNORMAL.equals(status)) {
            return SOURCE_STATUS_ABNORMAL;
        }
        return SOURCE_STATUS_RUNNING;
    }

    private long normalizePageValue(Long value, long defaultValue) {
        return value == null || value < 1 ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
