package com.haidian.intel.platform;

import com.haidian.intel.platform.modules.collect.mapper.CollectionTaskMapper;
import com.haidian.intel.platform.modules.dashboard.mapper.FocusTargetMapper;
import com.haidian.intel.platform.modules.dashboard.mapper.HotspotTopicMapper;
import com.haidian.intel.platform.modules.intel.mapper.EventTypeMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelItemMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelItemTagMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelSourceMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelTagMapper;
import com.haidian.intel.platform.modules.region.mapper.MapLayerMapper;
import com.haidian.intel.platform.modules.region.mapper.RegionGeoMapper;
import com.haidian.intel.platform.modules.region.mapper.RegionMapper;
import com.haidian.intel.platform.modules.source.mapper.CollectionSourceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 基础上下文加载测试固定使用 test 环境，避免本地未准备 MySQL 时影响持续验证。
 */
@SpringBootTest
@ActiveProfiles("test")
class HaidianIntelPlatformApplicationTests {

    @MockitoBean
    private RegionMapper regionMapper;

    @MockitoBean
    private RegionGeoMapper regionGeoMapper;

    @MockitoBean
    private MapLayerMapper mapLayerMapper;

    @MockitoBean
    private IntelItemMapper intelItemMapper;

    @MockitoBean
    private IntelSourceMapper intelSourceMapper;

    @MockitoBean
    private IntelTagMapper intelTagMapper;

    @MockitoBean
    private IntelItemTagMapper intelItemTagMapper;

    @MockitoBean
    private EventTypeMapper eventTypeMapper;

    @MockitoBean
    private HotspotTopicMapper hotspotTopicMapper;

    @MockitoBean
    private FocusTargetMapper focusTargetMapper;

    @MockitoBean
    private CollectionSourceMapper collectionSourceMapper;

    @MockitoBean
    private CollectionTaskMapper collectionTaskMapper;

    /**
     * 验证 Spring Boot 基础上下文可以完成启动。
     */
    @Test
    void contextLoads() {
    }
}
