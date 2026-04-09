package com.haidian.intel.platform.modules.region.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.intel.entity.EventType;
import com.haidian.intel.platform.modules.intel.entity.IntelItem;
import com.haidian.intel.platform.modules.intel.mapper.EventTypeMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelItemMapper;
import com.haidian.intel.platform.modules.region.dto.MapPointQueryDTO;
import com.haidian.intel.platform.modules.region.entity.Region;
import com.haidian.intel.platform.modules.region.mapper.RegionMapper;
import com.haidian.intel.platform.modules.region.service.MapPointService;
import com.haidian.intel.platform.modules.region.vo.MapPointVO;
import java.time.LocalDateTime;
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
 * 地图点位服务实现。
 * 当前直接基于情报主表查询，先满足首页地图点位真实数据联调。
 */
@Service
@RequiredArgsConstructor
public class MapPointServiceImpl implements MapPointService {

    private static final int HOME_VISIBLE_STATUS = 1;
    private static final int HOME_VISIBLE_REVIEW_STATUS = 1;

    private final IntelItemMapper intelItemMapper;
    private final EventTypeMapper eventTypeMapper;
    private final RegionMapper regionMapper;

    @Override
    public List<MapPointVO> listMapPoints(MapPointQueryDTO queryDTO) {
        MapPointQueryDTO actualQuery = queryDTO == null ? new MapPointQueryDTO() : queryDTO;
        validateTimeRange(actualQuery.getStartTime(), actualQuery.getEndTime());
        validateScopeFilters(actualQuery.getRegionId(), actualQuery.getEventTypeId());

        List<IntelItem> items = intelItemMapper.selectList(buildMapPointQuery(actualQuery));
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        RelationData relationData = loadRelationData(items);
        return items.stream()
                .map(item -> toMapPointVO(item, relationData))
                .toList();
    }

    /**
     * 地图点位只保留首页可见且带坐标的数据，避免前端渲染空点位。
     */
    private LambdaQueryWrapper<IntelItem> buildMapPointQuery(MapPointQueryDTO queryDTO) {
        LambdaQueryWrapper<IntelItem> queryWrapper = Wrappers.<IntelItem>lambdaQuery();
        queryWrapper.eq(IntelItem::getStatus, HOME_VISIBLE_STATUS);
        queryWrapper.eq(IntelItem::getReviewStatus, HOME_VISIBLE_REVIEW_STATUS);
        queryWrapper.eq(queryDTO.getRegionId() != null, IntelItem::getRegionId, queryDTO.getRegionId());
        queryWrapper.eq(queryDTO.getEventTypeId() != null, IntelItem::getEventTypeId, queryDTO.getEventTypeId());
        queryWrapper.isNotNull(IntelItem::getLng);
        queryWrapper.isNotNull(IntelItem::getLat);
        applyEffectiveTimeRange(queryWrapper, queryDTO.getStartTime(), queryDTO.getEndTime());
        queryWrapper.orderByDesc(
                IntelItem::getFocusFlag,
                IntelItem::getHeatScore,
                IntelItem::getOccurTime,
                IntelItem::getPublishTime,
                IntelItem::getId
        );
        return queryWrapper;
    }

    /**
     * 时间范围优先以事件发生时间过滤；若发生时间缺失，则回退到发布时间。
     */
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

    private RelationData loadRelationData(List<IntelItem> items) {
        Set<Long> regionIds = collectIds(items, IntelItem::getRegionId);
        Set<Long> eventTypeIds = collectIds(items, IntelItem::getEventTypeId);

        Map<Long, Region> regionMap = regionIds.isEmpty()
                ? Collections.emptyMap()
                : regionMapper.selectBatchIds(regionIds).stream()
                        .collect(Collectors.toMap(Region::getId, Function.identity()));

        Map<Long, EventType> eventTypeMap = eventTypeIds.isEmpty()
                ? Collections.emptyMap()
                : eventTypeMapper.selectBatchIds(eventTypeIds).stream()
                        .collect(Collectors.toMap(EventType::getId, Function.identity()));

        return new RelationData(regionMap, eventTypeMap);
    }

    private MapPointVO toMapPointVO(IntelItem intelItem, RelationData relationData) {
        Region region = relationData.regionMap().get(intelItem.getRegionId());
        EventType eventType = relationData.eventTypeMap().get(intelItem.getEventTypeId());

        MapPointVO pointVO = new MapPointVO();
        pointVO.setId(intelItem.getId());
        pointVO.setTitle(intelItem.getTitle());
        pointVO.setRegionId(intelItem.getRegionId());
        pointVO.setRegion(region == null ? null : region.getRegionName());
        pointVO.setEventTypeId(intelItem.getEventTypeId());
        pointVO.setType(eventType == null ? null : eventType.getEventName());
        pointVO.setCategory(eventType == null ? null : eventType.getEventCategory());
        pointVO.setLng(intelItem.getLng());
        pointVO.setLat(intelItem.getLat());
        pointVO.setCoords(List.of(intelItem.getLng(), intelItem.getLat()));
        pointVO.setHeat(defaultInteger(intelItem.getHeatScore()));
        pointVO.setImportanceLevel(defaultInteger(intelItem.getImportanceLevel()));
        pointVO.setRiskLevel(defaultInteger(intelItem.getRiskLevel()));
        pointVO.setFocusFlag(defaultInteger(intelItem.getFocusFlag()));
        pointVO.setTime(resolvePointTime(intelItem));
        pointVO.setSummary(intelItem.getSummary());
        return pointVO;
    }

    private LocalDateTime resolvePointTime(IntelItem intelItem) {
        return intelItem.getOccurTime() != null ? intelItem.getOccurTime() : intelItem.getPublishTime();
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private Set<Long> collectIds(Collection<IntelItem> items, Function<IntelItem, Long> idGetter) {
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

    private void validateScopeFilters(Long regionId, Long eventTypeId) {
        if (regionId != null && !isEnabledRegion(regionId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "地区不存在或已停用");
        }
        if (eventTypeId != null && !isEnabledEventType(eventTypeId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "事件类型不存在或已停用");
        }
    }

    private boolean isEnabledRegion(Long regionId) {
        return regionMapper.selectOne(
                Wrappers.<Region>lambdaQuery()
                        .eq(Region::getId, regionId)
                        .eq(Region::getEnabled, 1)
                        .last("LIMIT 1")
        ) != null;
    }

    private boolean isEnabledEventType(Long eventTypeId) {
        return eventTypeMapper.selectOne(
                Wrappers.<EventType>lambdaQuery()
                        .eq(EventType::getId, eventTypeId)
                        .eq(EventType::getEnabled, 1)
                        .last("LIMIT 1")
        ) != null;
    }

    private record RelationData(
            Map<Long, Region> regionMap,
            Map<Long, EventType> eventTypeMap
    ) {
    }
}
