package com.haidian.intel.platform.modules.collect.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskPageQueryDTO;
import com.haidian.intel.platform.modules.collect.entity.CollectionTask;
import com.haidian.intel.platform.modules.collect.mapper.CollectionTaskMapper;
import com.haidian.intel.platform.modules.collect.vo.CollectionTaskDetailVO;
import com.haidian.intel.platform.modules.collect.vo.CollectionTaskPageVO;
import com.haidian.intel.platform.modules.source.entity.CollectionSource;
import com.haidian.intel.platform.modules.source.mapper.CollectionSourceMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for collection task management service.
 */
@ExtendWith(MockitoExtension.class)
class CollectionTaskServiceImplTest {

    @Mock
    private CollectionTaskMapper collectionTaskMapper;

    @Mock
    private CollectionSourceMapper collectionSourceMapper;

    private CollectionTaskServiceImpl collectionTaskService;

    @BeforeEach
    void setUp() {
        collectionTaskService = new CollectionTaskServiceImpl(collectionTaskMapper, collectionSourceMapper);
    }

    @Test
    void shouldPageTasksWithSourceRelation() {
        Page<CollectionTask> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(buildRunningTask(8101L)));
        when(collectionTaskMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(collectionSourceMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildEnabledSource()));

        PageResponse<CollectionTaskPageVO> response = collectionTaskService.pageTasks(
                new CollectionTaskPageQueryDTO()
        );

        assertThat(response.getTotal()).isEqualTo(1L);
        assertThat(response.getRecords()).hasSize(1);
        CollectionTaskPageVO record = response.getRecords().getFirst();
        assertThat(record.getTaskName()).isEqualTo("政务交换库增量同步");
        assertThat(record.getSourceName()).isEqualTo("政务交换库");
        assertThat(record.getTaskTypeLabel()).isEqualTo("增量采集");
        assertThat(record.getStatus()).isEqualTo("running");
        assertThat(record.getStatusLabel()).isEqualTo("运行中");
        assertThat(record.getLatestResultLabel()).isEqualTo("成功");
    }

    @Test
    void shouldEnablePausedTaskWhenSourceEnabled() {
        CollectionTask pausedTask = buildPausedTask(8102L);
        CollectionTask enabledTask = buildRunningTask(8102L);

        when(collectionTaskMapper.selectById(8102L)).thenReturn(pausedTask, enabledTask);
        when(collectionSourceMapper.selectById(7001L)).thenReturn(buildEnabledSource());

        CollectionTaskDetailVO detailVO = collectionTaskService.enableTask(8102L);

        assertThat(detailVO.getId()).isEqualTo(8102L);
        assertThat(detailVO.getEnabled()).isEqualTo(1);
        assertThat(detailVO.getStatus()).isEqualTo("running");
        assertThat(detailVO.getStatusLabel()).isEqualTo("运行中");
    }

    @Test
    void shouldRejectEnableTaskWhenSourceDisabled() {
        when(collectionTaskMapper.selectById(8102L)).thenReturn(buildPausedTask(8102L));
        when(collectionSourceMapper.selectById(7001L)).thenReturn(buildDisabledSource());

        assertThatThrownBy(() -> collectionTaskService.enableTask(8102L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("数据源未启用，无法启用采集任务");
    }

    private CollectionTask buildRunningTask(Long taskId) {
        CollectionTask task = new CollectionTask();
        task.setId(taskId);
        task.setTaskName("政务交换库增量同步");
        task.setSourceId(7001L);
        task.setTaskType("incremental_collect");
        task.setCronExpr("0 */30 * * * ?");
        task.setFrequencyDesc("每30分钟");
        task.setEnabled(1);
        task.setRunStatus("running");
        task.setLatestResult("success");
        task.setLatestRunTime(LocalDateTime.of(2026, 4, 8, 9, 30));
        task.setRecordCount(24830L);
        task.setSuccessRate(new BigDecimal("99.60"));
        task.setDescription("Task-08 task page test");
        task.setCreatedTime(LocalDateTime.of(2026, 4, 8, 8, 0));
        task.setUpdatedTime(LocalDateTime.of(2026, 4, 8, 9, 30));
        return task;
    }

    private CollectionTask buildPausedTask(Long taskId) {
        CollectionTask task = new CollectionTask();
        task.setId(taskId);
        task.setTaskName("政务交换库增量同步");
        task.setSourceId(7001L);
        task.setTaskType("incremental_collect");
        task.setCronExpr("0 */30 * * * ?");
        task.setFrequencyDesc("每30分钟");
        task.setEnabled(0);
        task.setRunStatus("paused");
        task.setLatestResult("success");
        task.setLatestRunTime(LocalDateTime.of(2026, 4, 8, 9, 0));
        task.setRecordCount(24830L);
        task.setSuccessRate(new BigDecimal("99.60"));
        task.setDescription("Task-08 paused task test");
        return task;
    }

    private CollectionSource buildEnabledSource() {
        CollectionSource source = new CollectionSource();
        source.setId(7001L);
        source.setSourceName("政务交换库");
        source.setEnabled(1);
        return source;
    }

    private CollectionSource buildDisabledSource() {
        CollectionSource source = new CollectionSource();
        source.setId(7001L);
        source.setSourceName("政务交换库");
        source.setEnabled(0);
        return source;
    }
}
