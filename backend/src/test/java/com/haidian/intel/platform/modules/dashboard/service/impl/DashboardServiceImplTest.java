package com.haidian.intel.platform.modules.dashboard.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import com.haidian.intel.platform.modules.dashboard.dto.DashboardFocusTargetQueryDTO;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardHotspotQueryDTO;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardStatsQueryDTO;
import com.haidian.intel.platform.modules.dashboard.entity.FocusTarget;
import com.haidian.intel.platform.modules.dashboard.entity.HotspotTopic;
import com.haidian.intel.platform.modules.dashboard.mapper.FocusTargetMapper;
import com.haidian.intel.platform.modules.dashboard.mapper.HotspotTopicMapper;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardFocusTargetVO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardHotspotVO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardOverviewVO;
import com.haidian.intel.platform.modules.dashboard.vo.DashboardRegionSummaryVO;
import com.haidian.intel.platform.modules.intel.entity.EventType;
import com.haidian.intel.platform.modules.intel.entity.IntelItem;
import com.haidian.intel.platform.modules.intel.mapper.EventTypeMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelItemMapper;
import com.haidian.intel.platform.modules.region.entity.Region;
import com.haidian.intel.platform.modules.region.mapper.RegionMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 首页概览服务单元测试。
 * 覆盖概览卡片、地区态势、热点专题和重点关注对象的首页联调能力。
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private IntelItemMapper intelItemMapper;

    @Mock
    private EventTypeMapper eventTypeMapper;

    @Mock
    private RegionMapper regionMapper;

    @Mock
    private HotspotTopicMapper hotspotTopicMapper;

    @Mock
    private FocusTargetMapper focusTargetMapper;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                intelItemMapper,
                eventTypeMapper,
                regionMapper,
                hotspotTopicMapper,
                focusTargetMapper
        );
    }

    @Test
    void shouldBuildOverviewAndRegionSummary() {
        when(regionMapper.selectOne(any())).thenReturn(buildRegion(3001L, "海淀区"));
        when(intelItemMapper.selectList(any())).thenReturn(List.of(
                buildIntelItem(9001L, 4001L, 0, LocalDateTime.of(2026, 4, 7, 9, 0)),
                buildIntelItem(9002L, 4002L, 1, LocalDateTime.of(2026, 4, 7, 10, 0)),
                buildIntelItem(9003L, 4003L, 0, LocalDateTime.of(2026, 4, 7, 11, 0)),
                buildIntelItem(9004L, 4004L, 0, LocalDateTime.of(2026, 4, 7, 8, 0))
        ));
        when(eventTypeMapper.selectBatchIds(anyCollection())).thenReturn(List.of(
                buildEventType(4001L, "风险事件", "risk"),
                buildEventType(4002L, "突发事件", "emergency"),
                buildEventType(4003L, "热点事件", "hotspot"),
                buildEventType(4004L, "重点关注", "focus")
        ));

        DashboardStatsQueryDTO queryDTO = new DashboardStatsQueryDTO();
        queryDTO.setRegionId(3001L);

        DashboardOverviewVO overview = dashboardService.getOverview(queryDTO);
        DashboardRegionSummaryVO summary = dashboardService.getRegionSummary(queryDTO);

        assertThat(overview.getRegionName()).isEqualTo("海淀区");
        assertThat(overview.getTotalEventCount()).isEqualTo(4L);
        assertThat(overview.getRiskEventCount()).isEqualTo(1L);
        assertThat(overview.getEmergencyEventCount()).isEqualTo(1L);
        assertThat(overview.getHotspotEventCount()).isEqualTo(1L);
        assertThat(overview.getFocusCount()).isEqualTo(2L);
        assertThat(overview.getLatestUpdateTime()).isEqualTo(LocalDateTime.of(2026, 4, 7, 11, 0));

        assertThat(summary.getSummary()).contains("海淀区当前共监测到4条事件");
        assertThat(summary.getSummary()).contains("风险事件1条");
        assertThat(summary.getSummary()).contains("突发事件1条");
        assertThat(summary.getSummary()).contains("热点事件1条");
        assertThat(summary.getSummary()).contains("重点关注2条");
    }

    @Test
    void shouldReturnEmptySummaryWhenNoData() {
        when(intelItemMapper.selectList(any())).thenReturn(List.of());

        DashboardRegionSummaryVO summary = dashboardService.getRegionSummary(new DashboardStatsQueryDTO());

        assertThat(summary.getRegionName()).isEqualTo("全区");
        assertThat(summary.getTotalEventCount()).isZero();
        assertThat(summary.getSummary()).isEqualTo("全区当前暂无符合条件的事件数据。");
        assertThat(summary.getLatestUpdateTime()).isNull();
    }

    @Test
    void shouldReturnHotspotsOrderedForHomepage() {
        when(hotspotTopicMapper.selectList(any())).thenReturn(List.of(
                buildHotspotTopic(8001L, "中关村创新要素流动", "创新专题", 3004L, 96, 1340,
                        LocalDateTime.of(2026, 4, 8, 9, 10), "创新研判中心", "中关村创新要素流动"),
                buildHotspotTopic(8002L, "学院路科研协同观察", "科研观察", 3005L, 86, 712,
                        LocalDateTime.of(2026, 4, 8, 8, 40), "科研协同组", "学院路科研协同")
        ));
        when(regionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(
                buildRegion(3004L, "中关村街道"),
                buildRegion(3005L, "学院路街道")
        ));

        List<DashboardHotspotVO> hotspots = dashboardService.listHotspots(new DashboardHotspotQueryDTO());

        assertThat(hotspots).hasSize(2);
        assertThat(hotspots.getFirst().getTitle()).isEqualTo("中关村创新要素流动");
        assertThat(hotspots.getFirst().getHeat()).isEqualTo(96);
        assertThat(hotspots.getFirst().getMentions()).isEqualTo(1340);
        assertThat(hotspots.getFirst().getRegionName()).isEqualTo("中关村街道");
        assertThat(hotspots.getFirst().getDepartment()).isEqualTo("创新研判中心");
        assertThat(hotspots.getFirst().getKeyword()).isEqualTo("中关村创新要素流动");
    }

    @Test
    void shouldReturnFocusTargetsOrderedForHomepage() {
        when(focusTargetMapper.selectList(any())).thenReturn(List.of(
                buildFocusTarget(8101L, "中关村科学城重点机构", "重点机构", 3004L, 3,
                        LocalDateTime.of(2026, 4, 8, 9, 6), "中关村论坛分会场临时管控", "连续盯防", "中关村科学城重点机构"),
                buildFocusTarget(8103L, "上地软件园龙头企业", "重点企业", 3003L, 2,
                        LocalDateTime.of(2026, 4, 8, 8, 31), "上地算力调度异常预警", "持续观察", "上地软件园龙头企业")
        ));
        when(regionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(
                buildRegion(3004L, "中关村街道"),
                buildRegion(3003L, "上地街道")
        ));

        List<DashboardFocusTargetVO> focusTargets = dashboardService.listFocusTargets(new DashboardFocusTargetQueryDTO());

        assertThat(focusTargets).hasSize(2);
        assertThat(focusTargets.getFirst().getTitle()).isEqualTo("中关村科学城重点机构");
        assertThat(focusTargets.getFirst().getLevel()).isEqualTo(3);
        assertThat(focusTargets.getFirst().getLevelLabel()).isEqualTo("高");
        assertThat(focusTargets.getFirst().getRegionName()).isEqualTo("中关村街道");
        assertThat(focusTargets.getFirst().getLatestEventTitle()).isEqualTo("中关村论坛分会场临时管控");
        assertThat(focusTargets.getFirst().getStatus()).isEqualTo("连续盯防");
    }

    private Region buildRegion(Long regionId, String regionName) {
        Region region = new Region();
        region.setId(regionId);
        region.setRegionName(regionName);
        region.setEnabled(1);
        return region;
    }

    private EventType buildEventType(Long eventTypeId, String eventName, String eventCategory) {
        EventType eventType = new EventType();
        eventType.setId(eventTypeId);
        eventType.setEventName(eventName);
        eventType.setEventCategory(eventCategory);
        eventType.setEnabled(1);
        return eventType;
    }

    private IntelItem buildIntelItem(Long intelId, Long eventTypeId, Integer focusFlag, LocalDateTime updatedTime) {
        IntelItem intelItem = new IntelItem();
        intelItem.setId(intelId);
        intelItem.setRegionId(3001L);
        intelItem.setEventTypeId(eventTypeId);
        intelItem.setFocusFlag(focusFlag);
        intelItem.setUpdatedTime(updatedTime);
        intelItem.setPublishTime(updatedTime.minusMinutes(20));
        intelItem.setOccurTime(updatedTime.minusMinutes(40));
        return intelItem;
    }

    private HotspotTopic buildHotspotTopic(
            Long topicId,
            String topicName,
            String topicCategory,
            Long regionId,
            Integer heatScore,
            Integer mentionCount,
            LocalDateTime latestEventTime,
            String ownerDept,
            String keyword
    ) {
        HotspotTopic topic = new HotspotTopic();
        topic.setId(topicId);
        topic.setTopicName(topicName);
        topic.setTopicCategory(topicCategory);
        topic.setRegionId(regionId);
        topic.setHeatScore(heatScore);
        topic.setMentionCount(mentionCount);
        topic.setLatestEventTime(latestEventTime);
        topic.setOwnerDept(ownerDept);
        topic.setKeyword(keyword);
        topic.setSummary("Task-07 热点专题测试数据");
        topic.setEnabled(1);
        return topic;
    }

    private FocusTarget buildFocusTarget(
            Long targetId,
            String targetName,
            String targetType,
            Long regionId,
            Integer focusLevel,
            LocalDateTime latestEventTime,
            String latestEventTitle,
            String status,
            String keyword
    ) {
        FocusTarget target = new FocusTarget();
        target.setId(targetId);
        target.setTargetName(targetName);
        target.setTargetType(targetType);
        target.setRegionId(regionId);
        target.setFocusLevel(focusLevel);
        target.setLatestEventTime(latestEventTime);
        target.setLatestEventTitle(latestEventTitle);
        target.setStatus(status);
        target.setKeyword(keyword);
        target.setSummary("Task-07 重点关注测试数据");
        target.setEnabled(1);
        return target;
    }
}
