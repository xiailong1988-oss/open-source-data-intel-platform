package com.haidian.intel.platform.modules.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardFocusTargetQueryDTO;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardHotspotQueryDTO;
import com.haidian.intel.platform.modules.dashboard.dto.DashboardStatsQueryDTO;
import com.haidian.intel.platform.modules.dashboard.entity.FocusTarget;
import com.haidian.intel.platform.modules.dashboard.entity.HotspotTopic;
import com.haidian.intel.platform.modules.dashboard.mapper.FocusTargetMapper;
import com.haidian.intel.platform.modules.dashboard.mapper.HotspotTopicMapper;
import com.haidian.intel.platform.modules.dashboard.service.DashboardService;
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
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 首页概览服务实现。
 * 当前阶段直接基于情报主表做实时统计，优先保证首页联调可用与口径一致。
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int HOME_VISIBLE_STATUS = 1;
    private static final int HOME_VISIBLE_REVIEW_STATUS = 1;
    private static final String CATEGORY_RISK = "risk";
    private static final String CATEGORY_EMERGENCY = "emergency";
    private static final String CATEGORY_HOTSPOT = "hotspot";
    private static final String CATEGORY_FOCUS = "focus";
    private static final DateTimeFormatter SUMMARY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int DEFAULT_HOME_LIST_LIMIT = 6;

    private final IntelItemMapper intelItemMapper;
    private final EventTypeMapper eventTypeMapper;
    private final RegionMapper regionMapper;
    private final HotspotTopicMapper hotspotTopicMapper;
    private final FocusTargetMapper focusTargetMapper;

    @Override
    public DashboardOverviewVO getOverview(DashboardStatsQueryDTO queryDTO) {
        DashboardStatContext context = loadStatContext(queryDTO);
        return buildOverview(context);
    }

    @Override
    public DashboardRegionSummaryVO getRegionSummary(DashboardStatsQueryDTO queryDTO) {
        DashboardStatContext context = loadStatContext(queryDTO);
        DashboardOverviewVO overview = buildOverview(context);

        DashboardRegionSummaryVO summaryVO = new DashboardRegionSummaryVO();
        summaryVO.setRegionId(overview.getRegionId());
        summaryVO.setRegionName(overview.getRegionName());
        summaryVO.setSummary(buildSummaryText(context, overview));
        summaryVO.setTotalEventCount(overview.getTotalEventCount());
        summaryVO.setRiskEventCount(overview.getRiskEventCount());
        summaryVO.setEmergencyEventCount(overview.getEmergencyEventCount());
        summaryVO.setHotspotEventCount(overview.getHotspotEventCount());
        summaryVO.setFocusCount(overview.getFocusCount());
        summaryVO.setLatestUpdateTime(overview.getLatestUpdateTime());
        return summaryVO;
    }

    @Override
    public List<DashboardHotspotVO> listHotspots(DashboardHotspotQueryDTO queryDTO) {
        DashboardHotspotQueryDTO actualQuery = queryDTO == null ? new DashboardHotspotQueryDTO() : queryDTO;
        validateRegionFilter(actualQuery.getRegionId());

        List<HotspotTopic> topics = hotspotTopicMapper.selectList(buildHotspotQuery(actualQuery));
        if (topics == null || topics.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Region> regionMap = loadRegionMap(collectIds(topics, HotspotTopic::getRegionId));
        return topics.stream()
                .map(topic -> toDashboardHotspotVO(topic, regionMap.get(topic.getRegionId())))
                .toList();
    }

    @Override
    public List<DashboardFocusTargetVO> listFocusTargets(DashboardFocusTargetQueryDTO queryDTO) {
        DashboardFocusTargetQueryDTO actualQuery = queryDTO == null ? new DashboardFocusTargetQueryDTO() : queryDTO;
        validateRegionFilter(actualQuery.getRegionId());

        List<FocusTarget> targets = focusTargetMapper.selectList(buildFocusTargetQuery(actualQuery));
        if (targets == null || targets.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Region> regionMap = loadRegionMap(collectIds(targets, FocusTarget::getRegionId));
        return targets.stream()
                .map(target -> toDashboardFocusTargetVO(target, regionMap.get(target.getRegionId())))
                .toList();
    }

    private DashboardStatContext loadStatContext(DashboardStatsQueryDTO queryDTO) {
        DashboardStatsQueryDTO actualQuery = queryDTO == null ? new DashboardStatsQueryDTO() : queryDTO;
        validateTimeRange(actualQuery.getStartTime(), actualQuery.getEndTime());
        Region scopeRegion = validateRegionFilter(actualQuery.getRegionId());
        validateEventTypeFilter(actualQuery.getEventTypeId());

        List<IntelItem> items = intelItemMapper.selectList(buildDashboardQuery(actualQuery));
        Map<Long, EventType> eventTypeMap = loadEventTypeMap(items);
        String regionName = scopeRegion == null ? "全区" : scopeRegion.getRegionName();

        return new DashboardStatContext(actualQuery, regionName, items, eventTypeMap);
    }

    /**
     * 首页概览与地区态势统一复用首页可见数据口径，避免不同卡片统计结果互相打架。
     */
    private LambdaQueryWrapper<IntelItem> buildDashboardQuery(DashboardStatsQueryDTO queryDTO) {
        LambdaQueryWrapper<IntelItem> queryWrapper = Wrappers.<IntelItem>lambdaQuery();
        queryWrapper.eq(IntelItem::getStatus, HOME_VISIBLE_STATUS);
        queryWrapper.eq(IntelItem::getReviewStatus, HOME_VISIBLE_REVIEW_STATUS);
        queryWrapper.eq(queryDTO.getRegionId() != null, IntelItem::getRegionId, queryDTO.getRegionId());
        queryWrapper.eq(queryDTO.getEventTypeId() != null, IntelItem::getEventTypeId, queryDTO.getEventTypeId());
        applyEffectiveTimeRange(queryWrapper, queryDTO.getStartTime(), queryDTO.getEndTime());
        queryWrapper.orderByDesc(IntelItem::getUpdatedTime, IntelItem::getPublishTime, IntelItem::getId);
        return queryWrapper;
    }

    private void applyEffectiveTimeRange(
            LambdaQueryWrapper<IntelItem> queryWrapper,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        queryWrapper.and(
                startTime != null,
                wrapper -> wrapper.ge(IntelItem::getOccurTime, startTime)
                        .or(inner -> inner.isNull(IntelItem::getOccurTime)
                                .ge(IntelItem::getPublishTime, startTime))
        );
        queryWrapper.and(
                endTime != null,
                wrapper -> wrapper.le(IntelItem::getOccurTime, endTime)
                        .or(inner -> inner.isNull(IntelItem::getOccurTime)
                                .le(IntelItem::getPublishTime, endTime))
        );
    }

    private DashboardOverviewVO buildOverview(DashboardStatContext context) {
        List<IntelItem> items = context.items();
        Map<Long, EventType> eventTypeMap = context.eventTypeMap();

        DashboardOverviewVO overviewVO = new DashboardOverviewVO();
        overviewVO.setRegionId(context.query().getRegionId());
        overviewVO.setRegionName(context.regionName());
        overviewVO.setTotalEventCount((long) items.size());
        overviewVO.setRiskEventCount(countByCategory(items, eventTypeMap, CATEGORY_RISK));
        overviewVO.setEmergencyEventCount(countByCategory(items, eventTypeMap, CATEGORY_EMERGENCY));
        overviewVO.setHotspotEventCount(countByCategory(items, eventTypeMap, CATEGORY_HOTSPOT));
        overviewVO.setFocusCount(countFocus(items, eventTypeMap));
        overviewVO.setLatestUpdateTime(extractLatestUpdateTime(items));
        return overviewVO;
    }

    /**
     * 热点专题查询独立按热度排序，避免和概览统计或情报流查询逻辑混在一起。
     */
    private LambdaQueryWrapper<HotspotTopic> buildHotspotQuery(DashboardHotspotQueryDTO queryDTO) {
        LambdaQueryWrapper<HotspotTopic> queryWrapper = Wrappers.<HotspotTopic>lambdaQuery();
        queryWrapper.eq(HotspotTopic::getEnabled, 1);
        queryWrapper.eq(queryDTO.getRegionId() != null, HotspotTopic::getRegionId, queryDTO.getRegionId());
        queryWrapper.orderByDesc(HotspotTopic::getHeatScore, HotspotTopic::getLatestEventTime, HotspotTopic::getId);
        queryWrapper.last("LIMIT " + normalizeListLimit(queryDTO.getLimit()));
        return queryWrapper;
    }

    /**
     * 重点关注对象查询独立按等级和最近事件时间排序，保证首页右侧模块稳定可控。
     */
    private LambdaQueryWrapper<FocusTarget> buildFocusTargetQuery(DashboardFocusTargetQueryDTO queryDTO) {
        LambdaQueryWrapper<FocusTarget> queryWrapper = Wrappers.<FocusTarget>lambdaQuery();
        queryWrapper.eq(FocusTarget::getEnabled, 1);
        queryWrapper.eq(queryDTO.getRegionId() != null, FocusTarget::getRegionId, queryDTO.getRegionId());
        queryWrapper.orderByDesc(FocusTarget::getFocusLevel, FocusTarget::getLatestEventTime, FocusTarget::getId);
        queryWrapper.last("LIMIT " + normalizeListLimit(queryDTO.getLimit()));
        return queryWrapper;
    }

    private Long countByCategory(List<IntelItem> items, Map<Long, EventType> eventTypeMap, String category) {
        return items.stream()
                .filter(item -> category.equals(resolveEventCategory(item, eventTypeMap)))
                .count();
    }

    /**
     * 重点关注当前允许按 focusFlag 和 focus 分类双重近似，先服务首页概览卡片。
     */
    private Long countFocus(List<IntelItem> items, Map<Long, EventType> eventTypeMap) {
        return items.stream()
                .filter(item -> defaultInteger(item.getFocusFlag()) == 1
                        || CATEGORY_FOCUS.equals(resolveEventCategory(item, eventTypeMap)))
                .count();
    }

    private String resolveEventCategory(IntelItem item, Map<Long, EventType> eventTypeMap) {
        EventType eventType = eventTypeMap.get(item.getEventTypeId());
        return eventType == null ? null : eventType.getEventCategory();
    }

    private LocalDateTime extractLatestUpdateTime(List<IntelItem> items) {
        return items.stream()
                .map(this::resolveLatestUpdateTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime resolveLatestUpdateTime(IntelItem intelItem) {
        if (intelItem.getUpdatedTime() != null) {
            return intelItem.getUpdatedTime();
        }
        if (intelItem.getPublishTime() != null) {
            return intelItem.getPublishTime();
        }
        if (intelItem.getOccurTime() != null) {
            return intelItem.getOccurTime();
        }
        return intelItem.getCreatedTime();
    }

    private String buildSummaryText(DashboardStatContext context, DashboardOverviewVO overview) {
        if (overview.getTotalEventCount() == 0) {
            return context.regionName() + "当前暂无符合条件的事件数据。";
        }

        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append(context.regionName())
                .append("当前共监测到")
                .append(overview.getTotalEventCount())
                .append("条事件，风险事件")
                .append(overview.getRiskEventCount())
                .append("条、突发事件")
                .append(overview.getEmergencyEventCount())
                .append("条、热点事件")
                .append(overview.getHotspotEventCount())
                .append("条、重点关注")
                .append(overview.getFocusCount())
                .append("条");

        LocalDateTime latestUpdateTime = overview.getLatestUpdateTime();
        if (latestUpdateTime != null) {
            summaryBuilder.append("，最近更新时间为")
                    .append(latestUpdateTime.format(SUMMARY_TIME_FORMATTER));
        }
        summaryBuilder.append("。");
        return summaryBuilder.toString();
    }

    private DashboardHotspotVO toDashboardHotspotVO(HotspotTopic topic, Region region) {
        DashboardHotspotVO hotspotVO = new DashboardHotspotVO();
        hotspotVO.setId(topic.getId());
        hotspotVO.setTitle(topic.getTopicName());
        hotspotVO.setCategory(topic.getTopicCategory());
        hotspotVO.setRegionId(topic.getRegionId());
        hotspotVO.setRegionName(region == null ? null : region.getRegionName());
        hotspotVO.setHeat(defaultInteger(topic.getHeatScore()));
        hotspotVO.setMentions(defaultInteger(topic.getMentionCount()));
        hotspotVO.setUpdatedAt(topic.getLatestEventTime());
        hotspotVO.setDepartment(topic.getOwnerDept());
        hotspotVO.setKeyword(topic.getKeyword());
        hotspotVO.setSummary(topic.getSummary());
        hotspotVO.setDetailId(topic.getLatestIntelId());
        return hotspotVO;
    }

    private DashboardFocusTargetVO toDashboardFocusTargetVO(FocusTarget target, Region region) {
        DashboardFocusTargetVO focusTargetVO = new DashboardFocusTargetVO();
        focusTargetVO.setId(target.getId());
        focusTargetVO.setTitle(target.getTargetName());
        focusTargetVO.setTargetType(target.getTargetType());
        focusTargetVO.setRegionId(target.getRegionId());
        focusTargetVO.setRegionName(region == null ? null : region.getRegionName());
        focusTargetVO.setLevel(defaultInteger(target.getFocusLevel()));
        focusTargetVO.setLevelLabel(resolveFocusLevelLabel(target.getFocusLevel()));
        focusTargetVO.setLatestEventTime(target.getLatestEventTime());
        focusTargetVO.setLatestEventTitle(target.getLatestEventTitle());
        focusTargetVO.setStatus(target.getStatus());
        focusTargetVO.setKeyword(target.getKeyword());
        focusTargetVO.setSummary(target.getSummary());
        focusTargetVO.setDetailId(target.getLatestIntelId());
        return focusTargetVO;
    }

    private Map<Long, EventType> loadEventTypeMap(List<IntelItem> items) {
        Set<Long> eventTypeIds = collectIds(items, IntelItem::getEventTypeId);
        if (eventTypeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return eventTypeMapper.selectBatchIds(eventTypeIds).stream()
                .collect(Collectors.toMap(EventType::getId, Function.identity()));
    }

    private Map<Long, Region> loadRegionMap(Set<Long> regionIds) {
        if (regionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return regionMapper.selectBatchIds(regionIds).stream()
                .collect(Collectors.toMap(Region::getId, Function.identity()));
    }

    private <T> Set<Long> collectIds(Collection<T> items, Function<T, Long> idGetter) {
        return items.stream()
                .map(idGetter)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开始时间不能晚于结束时间");
        }
    }

    private Region validateRegionFilter(Long regionId) {
        if (regionId == null) {
            return null;
        }
        Region region = regionMapper.selectOne(
                Wrappers.<Region>lambdaQuery()
                        .eq(Region::getId, regionId)
                        .eq(Region::getEnabled, 1)
                        .last("LIMIT 1")
        );
        if (region == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "地区不存在或已停用");
        }
        return region;
    }

    private void validateEventTypeFilter(Long eventTypeId) {
        if (eventTypeId == null) {
            return;
        }
        EventType eventType = eventTypeMapper.selectOne(
                Wrappers.<EventType>lambdaQuery()
                        .eq(EventType::getId, eventTypeId)
                        .eq(EventType::getEnabled, 1)
                        .last("LIMIT 1")
        );
        if (eventType == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "事件类型不存在或已停用");
        }
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private int normalizeListLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_HOME_LIST_LIMIT;
        }
        return Math.min(limit, 20);
    }

    private String resolveFocusLevelLabel(Integer level) {
        if (level == null || level <= 1) {
            return "低";
        }
        if (level == 2) {
            return "中";
        }
        return "高";
    }

    private record DashboardStatContext(
            DashboardStatsQueryDTO query,
            String regionName,
            List<IntelItem> items,
            Map<Long, EventType> eventTypeMap
    ) {
    }
}
