package com.haidian.intel.platform.modules.region.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haidian.intel.platform.modules.region.dto.MapLayerQueryDTO;
import com.haidian.intel.platform.modules.region.entity.MapLayer;
import com.haidian.intel.platform.modules.region.mapper.MapLayerMapper;
import com.haidian.intel.platform.modules.region.vo.MapLayerVO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 地图图层服务单元测试。
 * 主要验证样式配置 JSON 会被正确解析为前端可直接读取的结构。
 */
@ExtendWith(MockitoExtension.class)
class MapLayerServiceImplTest {

    @Mock
    private MapLayerMapper mapLayerMapper;

    private MapLayerServiceImpl mapLayerService;

    @BeforeEach
    void setUp() {
        mapLayerService = new MapLayerServiceImpl(mapLayerMapper, new ObjectMapper());
    }

    @Test
    void shouldParseStyleConfigFromJson() {
        MapLayer mapLayer = new MapLayer();
        mapLayer.setId(5001L);
        mapLayer.setLayerCode("risk_event");
        mapLayer.setLayerName("风险事件图层");
        mapLayer.setLayerType("event");
        mapLayer.setEnabled(1);
        mapLayer.setSortNo(1);
        mapLayer.setExtJson("{\"icon\":\"warning-circle\",\"color\":\"#FF6B3B\",\"defaultVisible\":true}");

        when(mapLayerMapper.selectList(any())).thenReturn(List.of(mapLayer));

        List<MapLayerVO> layers = mapLayerService.getMapLayers(new MapLayerQueryDTO());

        assertThat(layers).hasSize(1);
        assertThat(layers.getFirst().getStyleConfig().path("icon").asText()).isEqualTo("warning-circle");
        assertThat(layers.getFirst().getStyleConfig().path("defaultVisible").asBoolean()).isTrue();
    }
}
