package com.haidian.intel.platform.modules.region.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import com.haidian.intel.platform.modules.intel.entity.EventType;
import com.haidian.intel.platform.modules.intel.entity.IntelItem;
import com.haidian.intel.platform.modules.intel.mapper.EventTypeMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelItemMapper;
import com.haidian.intel.platform.modules.region.dto.MapPointQueryDTO;
import com.haidian.intel.platform.modules.region.entity.Region;
import com.haidian.intel.platform.modules.region.mapper.RegionMapper;
import com.haidian.intel.platform.modules.region.vo.MapPointVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 地图点位服务单元测试。
 * 重点验证首页地图字段映射、坐标组装与关联名称回填。
 */
@ExtendWith(MockitoExtension.class)
class MapPointServiceImplTest {

    @Mock
    private IntelItemMapper intelItemMapper;

    @Mock
    private EventTypeMapper eventTypeMapper;

    @Mock
    private RegionMapper regionMapper;

    private MapPointServiceImpl mapPointService;

    @BeforeEach
    void setUp() {
        mapPointService = new MapPointServiceImpl(intelItemMapper, eventTypeMapper, regionMapper);
    }

    @Test
    void shouldReturnMapPointsForHomepage() {
        when(intelItemMapper.selectList(any())).thenReturn(List.of(buildIntelItem(9001L)));
        when(regionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildRegion()));
        when(eventTypeMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildEventType()));

        List<MapPointVO> points = mapPointService.listMapPoints(new MapPointQueryDTO());

        assertThat(points).hasSize(1);
        MapPointVO point = points.getFirst();
        assertThat(point.getId()).isEqualTo(9001L);
        assertThat(point.getTitle()).isEqualTo("校园周边群体活动");
        assertThat(point.getRegion()).isEqualTo("海淀区");
        assertThat(point.getType()).isEqualTo("风险事件");
        assertThat(point.getCategory()).isEqualTo("risk");
        assertThat(point.getCoords()).containsExactly(new BigDecimal("116.305000"), new BigDecimal("39.985000"));
        assertThat(point.getHeat()).isEqualTo(85);
        assertThat(point.getTime()).isEqualTo(LocalDateTime.of(2026, 4, 7, 9, 30));
        assertThat(point.getSummary()).isEqualTo("接到街道反馈，校园周边出现聚集活动。");
    }

    @Test
    void shouldReturnEmptyListWhenNoPointData() {
        when(intelItemMapper.selectList(any())).thenReturn(List.of());

        List<MapPointVO> points = mapPointService.listMapPoints(new MapPointQueryDTO());

        assertThat(points).isEmpty();
    }

    private IntelItem buildIntelItem(Long intelId) {
        IntelItem intelItem = new IntelItem();
        intelItem.setId(intelId);
        intelItem.setTitle("校园周边群体活动");
        intelItem.setSummary("接到街道反馈，校园周边出现聚集活动。");
        intelItem.setRegionId(3001L);
        intelItem.setEventTypeId(4001L);
        intelItem.setHeatScore(85);
        intelItem.setImportanceLevel(3);
        intelItem.setFocusFlag(1);
        intelItem.setRiskLevel(2);
        intelItem.setOccurTime(LocalDateTime.of(2026, 4, 7, 9, 30));
        intelItem.setPublishTime(LocalDateTime.of(2026, 4, 7, 10, 0));
        intelItem.setLng(new BigDecimal("116.305000"));
        intelItem.setLat(new BigDecimal("39.985000"));
        return intelItem;
    }

    private Region buildRegion() {
        Region region = new Region();
        region.setId(3001L);
        region.setRegionName("海淀区");
        region.setEnabled(1);
        return region;
    }

    private EventType buildEventType() {
        EventType eventType = new EventType();
        eventType.setId(4001L);
        eventType.setEventName("风险事件");
        eventType.setEventCategory("risk");
        eventType.setEnabled(1);
        return eventType;
    }
}
