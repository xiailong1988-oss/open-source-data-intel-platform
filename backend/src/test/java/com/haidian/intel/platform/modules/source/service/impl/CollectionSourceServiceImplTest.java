package com.haidian.intel.platform.modules.source.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.collect.mapper.CollectionTaskMapper;
import com.haidian.intel.platform.modules.source.dto.CollectionSourceCreateDTO;
import com.haidian.intel.platform.modules.source.dto.CollectionSourcePageQueryDTO;
import com.haidian.intel.platform.modules.source.entity.CollectionSource;
import com.haidian.intel.platform.modules.source.mapper.CollectionSourceMapper;
import com.haidian.intel.platform.modules.source.vo.CollectionSourceDetailVO;
import com.haidian.intel.platform.modules.source.vo.CollectionSourcePageVO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for collection source management service.
 */
@ExtendWith(MockitoExtension.class)
class CollectionSourceServiceImplTest {

    @Mock
    private CollectionSourceMapper collectionSourceMapper;

    @Mock
    private CollectionTaskMapper collectionTaskMapper;

    private CollectionSourceServiceImpl collectionSourceService;

    @BeforeEach
    void setUp() {
        collectionSourceService = new CollectionSourceServiceImpl(collectionSourceMapper, collectionTaskMapper);
    }

    @Test
    void shouldPageSourcesWithLabels() {
        Page<CollectionSource> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(buildSource(7001L)));
        when(collectionSourceMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResponse<CollectionSourcePageVO> response = collectionSourceService.pageSources(
                new CollectionSourcePageQueryDTO()
        );

        assertThat(response.getTotal()).isEqualTo(1L);
        assertThat(response.getRecords()).hasSize(1);
        CollectionSourcePageVO record = response.getRecords().getFirst();
        assertThat(record.getSourceName()).isEqualTo("政务交换库");
        assertThat(record.getSourceCategory()).isEqualTo("gov_system");
        assertThat(record.getSourceCategoryLabel()).isEqualTo("政务系统");
        assertThat(record.getAccessTypeLabel()).isEqualTo("数据库同步");
        assertThat(record.getStatus()).isEqualTo("running");
        assertThat(record.getStatusLabel()).isEqualTo("运行中");
    }

    @Test
    void shouldCreateSourceAndReturnDetail() {
        CollectionSourceCreateDTO createDTO = new CollectionSourceCreateDTO();
        createDTO.setSourceName("城市视频网关");
        createDTO.setSourceCategory("iot_device");
        createDTO.setSourceUrl("kafka://city-camera/topic/struct");
        createDTO.setAccessType("message_subscribe");
        createDTO.setEnabled(1);
        createDTO.setStatus("running");
        createDTO.setOwnerName("感知中心");
        createDTO.setRegionScope("海淀区");
        createDTO.setLatestCollectTime(LocalDateTime.of(2026, 4, 8, 9, 0));
        createDTO.setDescription("Task-08 source create test");
        createDTO.setRemark("remark");

        when(collectionSourceMapper.selectOne(any())).thenReturn(null);
        when(collectionSourceMapper.insert(any(CollectionSource.class))).thenAnswer(invocation -> {
            CollectionSource source = invocation.getArgument(0);
            source.setId(7002L);
            return 1;
        });
        when(collectionSourceMapper.selectById(7002L)).thenReturn(buildCreatedSource(7002L));

        CollectionSourceDetailVO detailVO = collectionSourceService.createSource(createDTO);

        assertThat(detailVO.getId()).isEqualTo(7002L);
        assertThat(detailVO.getSourceName()).isEqualTo("城市视频网关");
        assertThat(detailVO.getSourceCategoryLabel()).isEqualTo("物联网设备");
        assertThat(detailVO.getAccessTypeLabel()).isEqualTo("消息订阅");
        assertThat(detailVO.getStatusLabel()).isEqualTo("运行中");
    }

    @Test
    void shouldRejectDeletingSourceWhenTasksExist() {
        when(collectionSourceMapper.selectById(7001L)).thenReturn(buildSource(7001L));
        when(collectionTaskMapper.selectCount(any())).thenReturn(2L);

        assertThatThrownBy(() -> collectionSourceService.deleteSource(7001L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请先删除关联采集任务后再删除数据源");
    }

    private CollectionSource buildSource(Long sourceId) {
        CollectionSource source = new CollectionSource();
        source.setId(sourceId);
        source.setSourceName("政务交换库");
        source.setSourceCategory("gov_system");
        source.setSourceUrl("jdbc:mysql://gov-db/data_hub");
        source.setAccessType("database_sync");
        source.setEnabled(1);
        source.setStatus("running");
        source.setRegionScope("海淀区");
        source.setOwnerName("政务数据处");
        source.setLatestCollectTime(LocalDateTime.of(2026, 4, 8, 9, 12));
        source.setDescription("Task-08 source page test");
        source.setCreatedTime(LocalDateTime.of(2026, 4, 8, 8, 0));
        source.setUpdatedTime(LocalDateTime.of(2026, 4, 8, 9, 15));
        return source;
    }

    private CollectionSource buildCreatedSource(Long sourceId) {
        CollectionSource source = new CollectionSource();
        source.setId(sourceId);
        source.setSourceName("城市视频网关");
        source.setSourceCategory("iot_device");
        source.setSourceUrl("kafka://city-camera/topic/struct");
        source.setAccessType("message_subscribe");
        source.setEnabled(1);
        source.setStatus("running");
        source.setRegionScope("海淀区");
        source.setOwnerName("感知中心");
        source.setLatestCollectTime(LocalDateTime.of(2026, 4, 8, 9, 0));
        source.setDescription("Task-08 source create test");
        source.setRemark("remark");
        source.setCreatedTime(LocalDateTime.of(2026, 4, 8, 9, 0));
        source.setUpdatedTime(LocalDateTime.of(2026, 4, 8, 9, 0));
        return source;
    }
}
