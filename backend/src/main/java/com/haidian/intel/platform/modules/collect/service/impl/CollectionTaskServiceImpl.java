package com.haidian.intel.platform.modules.collect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskCreateDTO;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskPageQueryDTO;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskSaveDTO;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskUpdateDTO;
import com.haidian.intel.platform.modules.collect.entity.CollectionTask;
import com.haidian.intel.platform.modules.collect.mapper.CollectionTaskMapper;
import com.haidian.intel.platform.modules.collect.service.CollectionTaskService;
import com.haidian.intel.platform.modules.collect.vo.CollectionTaskDetailVO;
import com.haidian.intel.platform.modules.collect.vo.CollectionTaskPageVO;
import com.haidian.intel.platform.modules.source.entity.CollectionSource;
import com.haidian.intel.platform.modules.source.mapper.CollectionSourceMapper;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service implementation for collection task management.
 */
@Service
@RequiredArgsConstructor
public class CollectionTaskServiceImpl implements CollectionTaskService {

    private static final int DEFAULT_ENABLED = 1;
    private static final String RUN_STATUS_RUNNING = "running";
    private static final String RUN_STATUS_PAUSED = "paused";
    private static final String RUN_STATUS_ABNORMAL = "abnormal";
    private static final String RESULT_SUCCESS = "success";
    private static final String RESULT_FAILED = "failed";
    private static final String RESULT_PARTIAL_SUCCESS = "partial_success";

    private static final Map<String, String> TASK_TYPE_LABELS = Map.of(
            "incremental_collect", "增量采集",
            "full_sync", "全量同步",
            "realtime_subscribe", "实时订阅",
            "scheduled_check", "定时巡检"
    );

    private static final Map<String, String> RUN_STATUS_LABELS = Map.of(
            RUN_STATUS_RUNNING, "运行中",
            RUN_STATUS_PAUSED, "已暂停",
            RUN_STATUS_ABNORMAL, "异常"
    );

    private static final Map<String, String> RESULT_LABELS = Map.of(
            RESULT_SUCCESS, "成功",
            RESULT_FAILED, "失败",
            RESULT_PARTIAL_SUCCESS, "部分成功"
    );

    private final CollectionTaskMapper collectionTaskMapper;
    private final CollectionSourceMapper collectionSourceMapper;

    @Override
    public PageResponse<CollectionTaskPageVO> pageTasks(CollectionTaskPageQueryDTO queryDTO) {
        CollectionTaskPageQueryDTO actualQuery = queryDTO == null ? new CollectionTaskPageQueryDTO() : queryDTO;
        long pageNum = normalizePageValue(actualQuery.getPageNum(), 1L);
        long pageSize = normalizePageValue(actualQuery.getPageSize(), 10L);

        Set<Long> keywordSourceIds = findSourceIdsByKeyword(actualQuery.getKeyword());
        Page<CollectionTask> page = collectionTaskMapper.selectPage(
                new Page<>(pageNum, pageSize),
                buildPageQuery(actualQuery, keywordSourceIds)
        );
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return PageResponse.empty(pageNum, pageSize);
        }

        Map<Long, CollectionSource> sourceMap = loadSourceMap(page.getRecords());
        return new PageResponse<>(
                page.getRecords().stream().map(task -> toPageVO(task, sourceMap.get(task.getSourceId()))).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    @Override
    public CollectionTaskDetailVO getTaskDetail(Long taskId) {
        CollectionTask task = getTaskOrThrow(taskId);
        CollectionSource source = getSourceOrThrow(task.getSourceId());
        return toDetailVO(task, source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectionTaskDetailVO createTask(CollectionTaskCreateDTO createDTO) {
        validateTaskPayload(createDTO);
        assertUniqueTaskName(createDTO.getTaskName(), null);
        getSourceOrThrow(createDTO.getSourceId());

        CollectionTask task = new CollectionTask();
        applySaveDTO(createDTO, task);
        collectionTaskMapper.insert(task);
        if (task.getId() == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "采集任务新增后未返回主键");
        }
        return getTaskDetail(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectionTaskDetailVO updateTask(Long taskId, CollectionTaskUpdateDTO updateDTO) {
        CollectionTask existing = getTaskOrThrow(taskId);
        validateTaskPayload(updateDTO);
        assertUniqueTaskName(updateDTO.getTaskName(), taskId);
        getSourceOrThrow(updateDTO.getSourceId());

        applySaveDTO(updateDTO, existing);
        existing.setId(taskId);
        collectionTaskMapper.updateById(existing);
        return getTaskDetail(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId) {
        getTaskOrThrow(taskId);
        collectionTaskMapper.deleteById(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectionTaskDetailVO enableTask(Long taskId) {
        CollectionTask task = getTaskOrThrow(taskId);
        CollectionSource source = getSourceOrThrow(task.getSourceId());
        if (!Objects.equals(source.getEnabled(), 1)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "数据源未启用，无法启用采集任务");
        }

        task.setEnabled(1);
        if (!RUN_STATUS_ABNORMAL.equals(task.getRunStatus())) {
            task.setRunStatus(RUN_STATUS_RUNNING);
        }
        collectionTaskMapper.updateById(task);
        return getTaskDetail(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectionTaskDetailVO disableTask(Long taskId) {
        CollectionTask task = getTaskOrThrow(taskId);
        task.setEnabled(0);
        task.setRunStatus(RUN_STATUS_PAUSED);
        collectionTaskMapper.updateById(task);
        return getTaskDetail(taskId);
    }

    private LambdaQueryWrapper<CollectionTask> buildPageQuery(
            CollectionTaskPageQueryDTO queryDTO,
            Set<Long> keywordSourceIds
    ) {
        String keyword = StringUtils.trimWhitespace(queryDTO.getKeyword());
        LambdaQueryWrapper<CollectionTask> queryWrapper = Wrappers.<CollectionTask>lambdaQuery();
        queryWrapper.eq(queryDTO.getSourceId() != null, CollectionTask::getSourceId, queryDTO.getSourceId());
        queryWrapper.eq(StringUtils.hasText(queryDTO.getTaskType()), CollectionTask::getTaskType, queryDTO.getTaskType());
        queryWrapper.eq(queryDTO.getEnabled() != null, CollectionTask::getEnabled, queryDTO.getEnabled());
        applyStatusFilter(queryWrapper, queryDTO.getStatus());
        applyKeywordFilter(queryWrapper, keyword, keywordSourceIds);
        queryWrapper.orderByDesc(CollectionTask::getUpdatedTime, CollectionTask::getId);
        return queryWrapper;
    }

    private void applyStatusFilter(LambdaQueryWrapper<CollectionTask> queryWrapper, String status) {
        if (!StringUtils.hasText(status)) {
            return;
        }
        switch (status) {
            case RUN_STATUS_RUNNING -> {
                queryWrapper.eq(CollectionTask::getEnabled, 1);
                queryWrapper.eq(CollectionTask::getRunStatus, RUN_STATUS_RUNNING);
            }
            case RUN_STATUS_PAUSED -> queryWrapper.eq(CollectionTask::getEnabled, 0);
            case RUN_STATUS_ABNORMAL -> {
                queryWrapper.eq(CollectionTask::getEnabled, 1);
                queryWrapper.eq(CollectionTask::getRunStatus, RUN_STATUS_ABNORMAL);
            }
            default -> throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的采集任务状态");
        }
    }

    private void applyKeywordFilter(
            LambdaQueryWrapper<CollectionTask> queryWrapper,
            String keyword,
            Set<Long> keywordSourceIds
    ) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        queryWrapper.and(wrapper -> {
            wrapper.like(CollectionTask::getTaskName, keyword);
            if (!keywordSourceIds.isEmpty()) {
                wrapper.or().in(CollectionTask::getSourceId, keywordSourceIds);
            }
        });
    }

    private Set<Long> findSourceIdsByKeyword(String keyword) {
        String actualKeyword = StringUtils.trimWhitespace(keyword);
        if (!StringUtils.hasText(actualKeyword)) {
            return Collections.emptySet();
        }
        return collectionSourceMapper.selectList(
                        Wrappers.<CollectionSource>lambdaQuery().like(CollectionSource::getSourceName, actualKeyword)
                ).stream()
                .map(CollectionSource::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateTaskPayload(CollectionTaskSaveDTO saveDTO) {
        assertSupportedValue(saveDTO.getTaskType(), TASK_TYPE_LABELS, "不支持的采集任务类型");
        String normalizedRunStatus = normalizeRunStatus(saveDTO.getRunStatus(), normalizeEnabled(saveDTO.getEnabled()));
        assertSupportedValue(normalizedRunStatus, RUN_STATUS_LABELS, "不支持的运行状态");
        assertSupportedValue(normalizeLatestResult(saveDTO.getLatestResult()), RESULT_LABELS, "不支持的执行结果");
    }

    private void assertUniqueTaskName(String taskName, Long excludeId) {
        CollectionTask existing = collectionTaskMapper.selectOne(
                Wrappers.<CollectionTask>lambdaQuery()
                        .eq(CollectionTask::getTaskName, taskName)
                        .ne(excludeId != null, CollectionTask::getId, excludeId)
                        .last("LIMIT 1")
        );
        if (existing != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "采集任务名称已存在");
        }
    }

    private void applySaveDTO(CollectionTaskSaveDTO saveDTO, CollectionTask task) {
        int enabled = normalizeEnabled(saveDTO.getEnabled());
        task.setTaskName(saveDTO.getTaskName());
        task.setSourceId(saveDTO.getSourceId());
        task.setTaskType(saveDTO.getTaskType());
        task.setCronExpr(defaultString(saveDTO.getCronExpr()));
        task.setFrequencyDesc(saveDTO.getFrequencyDesc());
        task.setEnabled(enabled);
        task.setRunStatus(normalizeRunStatus(saveDTO.getRunStatus(), enabled));
        task.setLatestResult(normalizeLatestResult(saveDTO.getLatestResult()));
        task.setLatestRunTime(saveDTO.getLatestRunTime());
        task.setRecordCount(defaultLong(saveDTO.getRecordCount()));
        task.setSuccessRate(normalizeSuccessRate(saveDTO.getSuccessRate()));
        task.setDescription(defaultString(saveDTO.getDescription()));
        task.setRemark(defaultString(saveDTO.getRemark()));
    }

    private Map<Long, CollectionSource> loadSourceMap(Collection<CollectionTask> tasks) {
        Set<Long> sourceIds = tasks.stream()
                .map(CollectionTask::getSourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (sourceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return collectionSourceMapper.selectBatchIds(sourceIds).stream()
                .collect(Collectors.toMap(CollectionSource::getId, Function.identity()));
    }

    private CollectionTaskPageVO toPageVO(CollectionTask task, CollectionSource source) {
        CollectionTaskPageVO pageVO = new CollectionTaskPageVO();
        pageVO.setId(task.getId());
        pageVO.setTaskName(task.getTaskName());
        pageVO.setSourceId(task.getSourceId());
        pageVO.setSourceName(source == null ? null : source.getSourceName());
        pageVO.setTaskType(task.getTaskType());
        pageVO.setTaskTypeLabel(resolveLabel(TASK_TYPE_LABELS, task.getTaskType()));
        pageVO.setCronExpr(task.getCronExpr());
        pageVO.setFrequencyDesc(task.getFrequencyDesc());
        pageVO.setEnabled(task.getEnabled());
        pageVO.setRunStatus(task.getRunStatus());
        pageVO.setRunStatusLabel(resolveLabel(RUN_STATUS_LABELS, task.getRunStatus()));
        pageVO.setStatus(resolveTaskStatus(task));
        pageVO.setStatusLabel(resolveLabel(RUN_STATUS_LABELS, resolveTaskStatus(task)));
        pageVO.setLatestResult(task.getLatestResult());
        pageVO.setLatestResultLabel(resolveLabel(RESULT_LABELS, task.getLatestResult()));
        pageVO.setLatestRunTime(task.getLatestRunTime());
        pageVO.setRecordCount(task.getRecordCount());
        pageVO.setSuccessRate(task.getSuccessRate());
        return pageVO;
    }

    private CollectionTaskDetailVO toDetailVO(CollectionTask task, CollectionSource source) {
        CollectionTaskDetailVO detailVO = new CollectionTaskDetailVO();
        detailVO.setId(task.getId());
        detailVO.setTaskName(task.getTaskName());
        detailVO.setSourceId(task.getSourceId());
        detailVO.setSourceName(source == null ? null : source.getSourceName());
        detailVO.setTaskType(task.getTaskType());
        detailVO.setTaskTypeLabel(resolveLabel(TASK_TYPE_LABELS, task.getTaskType()));
        detailVO.setCronExpr(task.getCronExpr());
        detailVO.setFrequencyDesc(task.getFrequencyDesc());
        detailVO.setEnabled(task.getEnabled());
        detailVO.setRunStatus(task.getRunStatus());
        detailVO.setRunStatusLabel(resolveLabel(RUN_STATUS_LABELS, task.getRunStatus()));
        detailVO.setStatus(resolveTaskStatus(task));
        detailVO.setStatusLabel(resolveLabel(RUN_STATUS_LABELS, resolveTaskStatus(task)));
        detailVO.setLatestResult(task.getLatestResult());
        detailVO.setLatestResultLabel(resolveLabel(RESULT_LABELS, task.getLatestResult()));
        detailVO.setLatestRunTime(task.getLatestRunTime());
        detailVO.setRecordCount(task.getRecordCount());
        detailVO.setSuccessRate(task.getSuccessRate());
        detailVO.setDescription(task.getDescription());
        detailVO.setRemark(task.getRemark());
        detailVO.setCreatedTime(task.getCreatedTime());
        detailVO.setUpdatedTime(task.getUpdatedTime());
        return detailVO;
    }

    private String resolveTaskStatus(CollectionTask task) {
        if (!Objects.equals(task.getEnabled(), 1)) {
            return RUN_STATUS_PAUSED;
        }
        return RUN_STATUS_ABNORMAL.equals(task.getRunStatus()) ? RUN_STATUS_ABNORMAL : RUN_STATUS_RUNNING;
    }

    private CollectionTask getTaskOrThrow(Long taskId) {
        CollectionTask task = collectionTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "采集任务不存在");
        }
        return task;
    }

    private CollectionSource getSourceOrThrow(Long sourceId) {
        CollectionSource source = collectionSourceMapper.selectById(sourceId);
        if (source == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "关联数据源不存在");
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

    private String normalizeRunStatus(String runStatus, int enabled) {
        if (enabled == 0) {
            return RUN_STATUS_PAUSED;
        }
        if (RUN_STATUS_ABNORMAL.equals(runStatus)) {
            return RUN_STATUS_ABNORMAL;
        }
        return RUN_STATUS_RUNNING;
    }

    private String normalizeLatestResult(String latestResult) {
        if (!StringUtils.hasText(latestResult)) {
            return RESULT_SUCCESS;
        }
        return latestResult;
    }

    private BigDecimal normalizeSuccessRate(BigDecimal successRate) {
        return successRate == null ? BigDecimal.ZERO : successRate;
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private long normalizePageValue(Long value, long defaultValue) {
        return value == null || value < 1 ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
