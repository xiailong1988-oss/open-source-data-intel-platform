package com.haidian.intel.platform.modules.intel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.intel.dto.IntelCreateDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelPageQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelSaveDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelStreamQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelTickerQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelUpdateDTO;
import com.haidian.intel.platform.modules.intel.entity.EventType;
import com.haidian.intel.platform.modules.intel.entity.IntelItem;
import com.haidian.intel.platform.modules.intel.entity.IntelItemTag;
import com.haidian.intel.platform.modules.intel.entity.IntelSource;
import com.haidian.intel.platform.modules.intel.entity.IntelTag;
import com.haidian.intel.platform.modules.intel.mapper.EventTypeMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelItemMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelItemTagMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelSourceMapper;
import com.haidian.intel.platform.modules.intel.mapper.IntelTagMapper;
import com.haidian.intel.platform.modules.intel.service.IntelService;
import com.haidian.intel.platform.modules.intel.vo.IntelDetailVO;
import com.haidian.intel.platform.modules.intel.vo.IntelPageVO;
import com.haidian.intel.platform.modules.intel.vo.IntelStreamVO;
import com.haidian.intel.platform.modules.intel.vo.IntelTagVO;
import com.haidian.intel.platform.modules.intel.vo.IntelTickerVO;
import com.haidian.intel.platform.modules.region.entity.Region;
import com.haidian.intel.platform.modules.region.mapper.RegionMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 情报核心服务实现。
 * 当前围绕 biz_intel_item 提供后台管理 CRUD，以及首页快讯条和主情报流查询能力。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntelServiceImpl implements IntelService {

    private static final Comparator<IntelTagVO> INTEL_TAG_COMPARATOR = Comparator
            .comparing(IntelTagVO::getSortNo, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(IntelTagVO::getId, Comparator.nullsLast(Long::compareTo));

    private static final int HOME_VISIBLE_STATUS = 1;
    private static final int HOME_VISIBLE_REVIEW_STATUS = 1;
    private static final int TICKER_DEFAULT_LIMIT = 10;
    private static final int TICKER_RECENT_HOURS = 72;
    private static final int TICKER_MIN_IMPORTANCE_LEVEL = 2;
    private static final int TICKER_MIN_HEAT_SCORE = 60;

    private final IntelItemMapper intelItemMapper;
    private final IntelSourceMapper intelSourceMapper;
    private final IntelTagMapper intelTagMapper;
    private final IntelItemTagMapper intelItemTagMapper;
    private final EventTypeMapper eventTypeMapper;
    private final RegionMapper regionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResponse<IntelPageVO> pageIntel(IntelPageQueryDTO queryDTO) {
        IntelPageQueryDTO actualQuery = queryDTO == null ? new IntelPageQueryDTO() : queryDTO;
        validateTimeRange(actualQuery.getStartTime(), actualQuery.getEndTime());

        long pageNum = actualQuery.getPageNum() == null ? 1L : actualQuery.getPageNum();
        long pageSize = actualQuery.getPageSize() == null ? 20L : actualQuery.getPageSize();

        Page<IntelItem> page = intelItemMapper.selectPage(new Page<>(pageNum, pageSize), buildPageQuery(actualQuery));
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return PageResponse.empty(pageNum, pageSize);
        }

        RelationData relationData = loadRelationData(page.getRecords());
        List<IntelPageVO> records = page.getRecords().stream()
                .map(item -> toIntelPageVO(item, relationData))
                .toList();

        return new PageResponse<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<IntelTickerVO> listIntelTicker(IntelTickerQueryDTO queryDTO) {
        IntelTickerQueryDTO actualQuery = queryDTO == null ? new IntelTickerQueryDTO() : queryDTO;
        List<IntelItem> items = intelItemMapper.selectList(buildTickerQuery(actualQuery));
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        RelationData relationData = loadRelationData(items);
        return items.stream()
                .map(item -> toIntelTickerVO(item, relationData))
                .toList();
    }

    @Override
    public PageResponse<IntelStreamVO> pageIntelStream(IntelStreamQueryDTO queryDTO) {
        IntelStreamQueryDTO actualQuery = queryDTO == null ? new IntelStreamQueryDTO() : queryDTO;
        long pageNum = normalizePageValue(actualQuery.getPageNum(), 1L);
        long pageSize = normalizePageValue(actualQuery.getPageSize(), 10L);

        Page<IntelItem> page = intelItemMapper.selectPage(new Page<>(pageNum, pageSize), buildStreamQuery(actualQuery));
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            return PageResponse.empty(pageNum, pageSize);
        }

        RelationData relationData = loadRelationData(page.getRecords());
        List<IntelStreamVO> records = page.getRecords().stream()
                .map(item -> toIntelStreamVO(item, relationData))
                .toList();

        return new PageResponse<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public IntelDetailVO getIntelDetail(Long intelId) {
        IntelItem intelItem = getIntelItemOrThrow(intelId);
        RelationData relationData = loadRelationData(List.of(intelItem));
        return toIntelDetailVO(intelItem, relationData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IntelDetailVO createIntel(IntelCreateDTO createDTO) {
        validateReferences(createDTO);

        IntelItem intelItem = new IntelItem();
        applySaveDTO(createDTO, intelItem);
        intelItemMapper.insert(intelItem);
        if (intelItem.getId() == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "情报新增后未返回主键");
        }

        replaceTagRelations(intelItem.getId(), createDTO.getTagIds());
        return getIntelDetail(intelItem.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IntelDetailVO updateIntel(Long intelId, IntelUpdateDTO updateDTO) {
        IntelItem existing = getIntelItemOrThrow(intelId);
        validateReferences(updateDTO);

        applySaveDTO(updateDTO, existing);
        existing.setId(intelId);
        intelItemMapper.updateById(existing);

        replaceTagRelations(intelId, updateDTO.getTagIds());
        return getIntelDetail(intelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIntel(Long intelId) {
        getIntelItemOrThrow(intelId);
        intelItemMapper.deleteById(intelId);
        intelItemTagMapper.delete(Wrappers.<IntelItemTag>lambdaQuery().eq(IntelItemTag::getIntelItemId, intelId));
    }

    /**
     * 后台管理分页查询保留完整过滤能力，供管理端表格与联调使用。
     */
    private LambdaQueryWrapper<IntelItem> buildPageQuery(IntelPageQueryDTO queryDTO) {
        String keyword = StringUtils.trimWhitespace(queryDTO.getKeyword());
        LambdaQueryWrapper<IntelItem> queryWrapper = Wrappers.<IntelItem>lambdaQuery();
        queryWrapper.eq(queryDTO.getRegionId() != null, IntelItem::getRegionId, queryDTO.getRegionId());
        queryWrapper.eq(queryDTO.getEventTypeId() != null, IntelItem::getEventTypeId, queryDTO.getEventTypeId());
        queryWrapper.eq(queryDTO.getStatus() != null, IntelItem::getStatus, queryDTO.getStatus());
        queryWrapper.eq(queryDTO.getReviewStatus() != null, IntelItem::getReviewStatus, queryDTO.getReviewStatus());
        queryWrapper.ge(queryDTO.getStartTime() != null, IntelItem::getPublishTime, queryDTO.getStartTime());
        queryWrapper.le(queryDTO.getEndTime() != null, IntelItem::getPublishTime, queryDTO.getEndTime());
        applyKeywordFilter(queryWrapper, keyword);
        queryWrapper.orderByDesc(IntelItem::getPublishTime, IntelItem::getId);
        return queryWrapper;
    }

    /**
     * 快讯条查询与主情报流查询分开，确保“近期高优先级播报”和“按时间倒序列表”各自独立维护。
     */
    private LambdaQueryWrapper<IntelItem> buildTickerQuery(IntelTickerQueryDTO queryDTO) {
        String keyword = StringUtils.trimWhitespace(queryDTO.getKeyword());
        int limit = normalizeTickerLimit(queryDTO.getLimit());

        LambdaQueryWrapper<IntelItem> queryWrapper = Wrappers.<IntelItem>lambdaQuery();
        applyHomepageVisibleFilter(queryWrapper);
        applyHomepageBaseFilter(queryWrapper, queryDTO.getRegionId(), queryDTO.getEventTypeId(), keyword);
        queryWrapper.ge(IntelItem::getPublishTime, LocalDateTime.now().minusHours(TICKER_RECENT_HOURS));
        queryWrapper.and(wrapper -> wrapper.ge(IntelItem::getImportanceLevel, TICKER_MIN_IMPORTANCE_LEVEL)
                .or()
                .eq(IntelItem::getFocusFlag, 1)
                .or()
                .ge(IntelItem::getHeatScore, TICKER_MIN_HEAT_SCORE));
        queryWrapper.orderByDesc(
                IntelItem::getFocusFlag,
                IntelItem::getImportanceLevel,
                IntelItem::getHeatScore,
                IntelItem::getPublishTime,
                IntelItem::getId
        );
        queryWrapper.last("LIMIT " + limit);
        return queryWrapper;
    }

    private LambdaQueryWrapper<IntelItem> buildStreamQuery(IntelStreamQueryDTO queryDTO) {
        String keyword = StringUtils.trimWhitespace(queryDTO.getKeyword());
        LambdaQueryWrapper<IntelItem> queryWrapper = Wrappers.<IntelItem>lambdaQuery();
        applyHomepageVisibleFilter(queryWrapper);
        applyHomepageBaseFilter(queryWrapper, queryDTO.getRegionId(), queryDTO.getEventTypeId(), keyword);
        queryWrapper.orderByDesc(IntelItem::getPublishTime, IntelItem::getId);
        return queryWrapper;
    }

    private void applyHomepageVisibleFilter(LambdaQueryWrapper<IntelItem> queryWrapper) {
        queryWrapper.eq(IntelItem::getStatus, HOME_VISIBLE_STATUS);
        queryWrapper.eq(IntelItem::getReviewStatus, HOME_VISIBLE_REVIEW_STATUS);
    }

    private void applyHomepageBaseFilter(
            LambdaQueryWrapper<IntelItem> queryWrapper,
            Long regionId,
            Long eventTypeId,
            String keyword
    ) {
        queryWrapper.eq(regionId != null, IntelItem::getRegionId, regionId);
        queryWrapper.eq(eventTypeId != null, IntelItem::getEventTypeId, eventTypeId);
        applyKeywordFilter(queryWrapper, keyword);
    }

    private void applyKeywordFilter(LambdaQueryWrapper<IntelItem> queryWrapper, String keyword) {
        queryWrapper.and(
                StringUtils.hasText(keyword),
                wrapper -> wrapper.like(IntelItem::getTitle, keyword)
                        .or()
                        .like(IntelItem::getSummary, keyword)
                        .or()
                        .like(IntelItem::getAddress, keyword)
        );
    }

    /**
     * 写入时只允许绑定启用中的地区、事件类型、来源和标签，避免前端误选停用参考数据。
     */
    private void validateReferences(IntelSaveDTO saveDTO) {
        validateTimeRange(saveDTO.getOccurTime(), saveDTO.getPublishTime());
        if (!isEnabledRegion(saveDTO.getRegionId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "地区不存在或已停用");
        }
        if (!isEnabledEventType(saveDTO.getEventTypeId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "事件类型不存在或已停用");
        }
        if (!isEnabledSource(saveDTO.getSourceId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "来源不存在或已停用");
        }

        List<Long> tagIds = normalizeTagIds(saveDTO.getTagIds());
        if (!tagIds.isEmpty() && countEnabledTags(tagIds) != tagIds.size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "存在无效或已停用的标签ID");
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开始时间不能晚于结束时间");
        }
    }

    private void applySaveDTO(IntelSaveDTO saveDTO, IntelItem intelItem) {
        intelItem.setTitle(saveDTO.getTitle());
        intelItem.setSummary(saveDTO.getSummary());
        intelItem.setContent(saveDTO.getContent());
        intelItem.setRegionId(saveDTO.getRegionId());
        intelItem.setEventTypeId(saveDTO.getEventTypeId());
        intelItem.setSourceId(saveDTO.getSourceId());
        intelItem.setImportanceLevel(defaultInteger(saveDTO.getImportanceLevel(), 0));
        intelItem.setHeatScore(defaultInteger(saveDTO.getHeatScore(), 0));
        intelItem.setFocusFlag(defaultInteger(saveDTO.getFocusFlag(), 0));
        intelItem.setRiskLevel(defaultInteger(saveDTO.getRiskLevel(), 0));
        intelItem.setOccurTime(saveDTO.getOccurTime());
        intelItem.setPublishTime(saveDTO.getPublishTime());
        intelItem.setLng(saveDTO.getLng());
        intelItem.setLat(saveDTO.getLat());
        intelItem.setAddress(defaultString(saveDTO.getAddress()));
        intelItem.setStatus(defaultInteger(saveDTO.getStatus(), 1));
        intelItem.setReviewStatus(defaultInteger(saveDTO.getReviewStatus(), 0));
        intelItem.setDataOrigin(StringUtils.hasText(saveDTO.getDataOrigin()) ? saveDTO.getDataOrigin() : "manual");
        intelItem.setExtJson(serializeJson(saveDTO.getExtJson()));
        intelItem.setRemark(defaultString(saveDTO.getRemark()));
    }

    private void replaceTagRelations(Long intelId, List<Long> rawTagIds) {
        intelItemTagMapper.delete(Wrappers.<IntelItemTag>lambdaQuery().eq(IntelItemTag::getIntelItemId, intelId));
        for (Long tagId : normalizeTagIds(rawTagIds)) {
            IntelItemTag relation = new IntelItemTag();
            relation.setIntelItemId(intelId);
            relation.setTagId(tagId);
            intelItemTagMapper.insert(relation);
        }
    }

    private List<Long> normalizeTagIds(List<Long> rawTagIds) {
        if (rawTagIds == null || rawTagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(new LinkedHashSet<>(rawTagIds));
    }

    private RelationData loadRelationData(List<IntelItem> items) {
        Set<Long> regionIds = collectIds(items, IntelItem::getRegionId);
        Set<Long> eventTypeIds = collectIds(items, IntelItem::getEventTypeId);
        Set<Long> sourceIds = collectIds(items, IntelItem::getSourceId);
        List<Long> itemIds = items.stream().map(IntelItem::getId).filter(Objects::nonNull).toList();

        Map<Long, Region> regionMap = regionIds.isEmpty()
                ? Collections.emptyMap()
                : regionMapper.selectBatchIds(regionIds).stream()
                        .collect(Collectors.toMap(Region::getId, Function.identity()));

        Map<Long, EventType> eventTypeMap = eventTypeIds.isEmpty()
                ? Collections.emptyMap()
                : eventTypeMapper.selectBatchIds(eventTypeIds).stream()
                        .collect(Collectors.toMap(EventType::getId, Function.identity()));

        Map<Long, IntelSource> sourceMap = sourceIds.isEmpty()
                ? Collections.emptyMap()
                : intelSourceMapper.selectBatchIds(sourceIds).stream()
                        .collect(Collectors.toMap(IntelSource::getId, Function.identity()));

        return new RelationData(regionMap, eventTypeMap, sourceMap, loadIntelTagMap(itemIds));
    }

    private Map<Long, List<IntelTagVO>> loadIntelTagMap(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<IntelItemTag> relations = intelItemTagMapper.selectList(
                Wrappers.<IntelItemTag>lambdaQuery().in(IntelItemTag::getIntelItemId, itemIds)
        );
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> tagIds = relations.stream()
                .map(IntelItemTag::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, IntelTag> tagMap = intelTagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(IntelTag::getId, Function.identity()));

        Map<Long, List<IntelTagVO>> result = new LinkedHashMap<>();
        for (IntelItemTag relation : relations) {
            IntelTag tag = tagMap.get(relation.getTagId());
            if (tag == null) {
                continue;
            }
            result.computeIfAbsent(relation.getIntelItemId(), key -> new ArrayList<>()).add(toIntelTagVO(tag));
        }
        result.values().forEach(tags -> tags.sort(INTEL_TAG_COMPARATOR));
        return result;
    }

    private IntelPageVO toIntelPageVO(IntelItem intelItem, RelationData relationData) {
        Region region = relationData.regionMap().get(intelItem.getRegionId());
        EventType eventType = relationData.eventTypeMap().get(intelItem.getEventTypeId());
        IntelSource source = relationData.sourceMap().get(intelItem.getSourceId());

        IntelPageVO pageVO = new IntelPageVO();
        pageVO.setId(intelItem.getId());
        pageVO.setTitle(intelItem.getTitle());
        pageVO.setSummary(intelItem.getSummary());
        pageVO.setRegionId(intelItem.getRegionId());
        pageVO.setRegionName(region == null ? null : region.getRegionName());
        pageVO.setEventTypeId(intelItem.getEventTypeId());
        pageVO.setEventTypeName(eventType == null ? null : eventType.getEventName());
        pageVO.setEventCategory(eventType == null ? null : eventType.getEventCategory());
        pageVO.setSourceId(intelItem.getSourceId());
        pageVO.setSourceName(source == null ? null : source.getSourceName());
        pageVO.setImportanceLevel(intelItem.getImportanceLevel());
        pageVO.setHeatScore(intelItem.getHeatScore());
        pageVO.setFocusFlag(intelItem.getFocusFlag());
        pageVO.setRiskLevel(intelItem.getRiskLevel());
        pageVO.setOccurTime(intelItem.getOccurTime());
        pageVO.setPublishTime(intelItem.getPublishTime());
        pageVO.setLng(intelItem.getLng());
        pageVO.setLat(intelItem.getLat());
        pageVO.setAddress(intelItem.getAddress());
        pageVO.setStatus(intelItem.getStatus());
        pageVO.setReviewStatus(intelItem.getReviewStatus());
        pageVO.setDataOrigin(intelItem.getDataOrigin());
        pageVO.setTags(relationData.intelTagMap().getOrDefault(intelItem.getId(), Collections.emptyList()));
        return pageVO;
    }

    private IntelTickerVO toIntelTickerVO(IntelItem intelItem, RelationData relationData) {
        IntelTickerVO tickerVO = new IntelTickerVO();
        fillHomepageCommonFields(
                intelItem,
                relationData,
                tickerVO::setDetailId,
                tickerVO::setTitle,
                tickerVO::setTime,
                tickerVO::setRegion,
                tickerVO::setType,
                tickerVO::setSource,
                tickerVO::setSummary,
                tickerVO::setTags
        );
        return tickerVO;
    }

    private IntelStreamVO toIntelStreamVO(IntelItem intelItem, RelationData relationData) {
        IntelStreamVO streamVO = new IntelStreamVO();
        fillHomepageCommonFields(
                intelItem,
                relationData,
                streamVO::setDetailId,
                streamVO::setTitle,
                streamVO::setTime,
                streamVO::setRegion,
                streamVO::setType,
                streamVO::setSource,
                streamVO::setSummary,
                streamVO::setTags
        );
        return streamVO;
    }

    private void fillHomepageCommonFields(
            IntelItem intelItem,
            RelationData relationData,
            java.util.function.Consumer<Long> detailIdSetter,
            java.util.function.Consumer<String> titleSetter,
            java.util.function.Consumer<LocalDateTime> timeSetter,
            java.util.function.Consumer<String> regionSetter,
            java.util.function.Consumer<String> typeSetter,
            java.util.function.Consumer<String> sourceSetter,
            java.util.function.Consumer<String> summarySetter,
            java.util.function.Consumer<List<String>> tagsSetter
    ) {
        Region region = relationData.regionMap().get(intelItem.getRegionId());
        EventType eventType = relationData.eventTypeMap().get(intelItem.getEventTypeId());
        IntelSource source = relationData.sourceMap().get(intelItem.getSourceId());

        detailIdSetter.accept(intelItem.getId());
        titleSetter.accept(intelItem.getTitle());
        timeSetter.accept(intelItem.getPublishTime());
        regionSetter.accept(region == null ? null : region.getRegionName());
        typeSetter.accept(eventType == null ? null : eventType.getEventName());
        sourceSetter.accept(source == null ? null : source.getSourceName());
        summarySetter.accept(intelItem.getSummary());
        tagsSetter.accept(extractTagNames(intelItem.getId(), relationData));
    }

    private List<String> extractTagNames(Long intelId, RelationData relationData) {
        return relationData.intelTagMap()
                .getOrDefault(intelId, Collections.emptyList())
                .stream()
                .map(IntelTagVO::getTagName)
                .toList();
    }

    private IntelDetailVO toIntelDetailVO(IntelItem intelItem, RelationData relationData) {
        Region region = relationData.regionMap().get(intelItem.getRegionId());
        EventType eventType = relationData.eventTypeMap().get(intelItem.getEventTypeId());
        IntelSource source = relationData.sourceMap().get(intelItem.getSourceId());

        IntelDetailVO detailVO = new IntelDetailVO();
        detailVO.setId(intelItem.getId());
        detailVO.setTitle(intelItem.getTitle());
        detailVO.setSummary(intelItem.getSummary());
        detailVO.setContent(intelItem.getContent());
        detailVO.setRegionId(intelItem.getRegionId());
        detailVO.setRegionName(region == null ? null : region.getRegionName());
        detailVO.setEventTypeId(intelItem.getEventTypeId());
        detailVO.setEventCode(eventType == null ? null : eventType.getEventCode());
        detailVO.setEventTypeName(eventType == null ? null : eventType.getEventName());
        detailVO.setEventCategory(eventType == null ? null : eventType.getEventCategory());
        detailVO.setColorCode(eventType == null ? null : eventType.getColorCode());
        detailVO.setIconCode(eventType == null ? null : eventType.getIconCode());
        detailVO.setSourceId(intelItem.getSourceId());
        detailVO.setSourceName(source == null ? null : source.getSourceName());
        detailVO.setSourceType(source == null ? null : source.getSourceType());
        detailVO.setSourceUrl(source == null ? null : source.getSourceUrl());
        detailVO.setCredibilityLevel(source == null ? null : source.getCredibilityLevel());
        detailVO.setImportanceLevel(intelItem.getImportanceLevel());
        detailVO.setHeatScore(intelItem.getHeatScore());
        detailVO.setFocusFlag(intelItem.getFocusFlag());
        detailVO.setRiskLevel(intelItem.getRiskLevel());
        detailVO.setOccurTime(intelItem.getOccurTime());
        detailVO.setPublishTime(intelItem.getPublishTime());
        detailVO.setLng(intelItem.getLng());
        detailVO.setLat(intelItem.getLat());
        detailVO.setAddress(intelItem.getAddress());
        detailVO.setStatus(intelItem.getStatus());
        detailVO.setReviewStatus(intelItem.getReviewStatus());
        detailVO.setDataOrigin(intelItem.getDataOrigin());
        detailVO.setExtJson(parseJson(intelItem.getExtJson()));
        detailVO.setRemark(intelItem.getRemark());
        detailVO.setCreatedTime(intelItem.getCreatedTime());
        detailVO.setUpdatedTime(intelItem.getUpdatedTime());
        detailVO.setTags(relationData.intelTagMap().getOrDefault(intelItem.getId(), Collections.emptyList()));
        return detailVO;
    }

    private IntelTagVO toIntelTagVO(IntelTag tag) {
        IntelTagVO tagVO = new IntelTagVO();
        tagVO.setId(tag.getId());
        tagVO.setTagName(tag.getTagName());
        tagVO.setTagType(tag.getTagType());
        tagVO.setSortNo(tag.getSortNo());
        return tagVO;
    }

    private IntelItem getIntelItemOrThrow(Long intelId) {
        IntelItem intelItem = intelItemMapper.selectById(intelId);
        if (intelItem == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "情报不存在");
        }
        return intelItem;
    }

    private Set<Long> collectIds(Collection<IntelItem> items, Function<IntelItem, Long> idGetter) {
        return items.stream()
                .map(idGetter)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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

    private boolean isEnabledSource(Long sourceId) {
        return intelSourceMapper.selectOne(
                Wrappers.<IntelSource>lambdaQuery()
                        .eq(IntelSource::getId, sourceId)
                        .eq(IntelSource::getEnabled, 1)
                        .last("LIMIT 1")
        ) != null;
    }

    private long countEnabledTags(List<Long> tagIds) {
        return intelTagMapper.selectCount(
                Wrappers.<IntelTag>lambdaQuery()
                        .in(IntelTag::getId, tagIds)
                        .eq(IntelTag::getEnabled, 1)
        );
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private int normalizeTickerLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return TICKER_DEFAULT_LIMIT;
        }
        return Math.min(limit, 30);
    }

    private long normalizePageValue(Long value, long defaultValue) {
        return value == null || value < 1 ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String serializeJson(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize intel extJson.", exception);
            throw new BusinessException(ResultCode.BAD_REQUEST, "扩展字段格式不正确");
        }
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(Objects.requireNonNullElse(json, "{}"));
        } catch (JsonProcessingException exception) {
            log.error("Failed to parse intel extJson.", exception);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "情报扩展字段格式不正确");
        }
    }

    private record RelationData(
            Map<Long, Region> regionMap,
            Map<Long, EventType> eventTypeMap,
            Map<Long, IntelSource> sourceMap,
            Map<Long, List<IntelTagVO>> intelTagMap
    ) {
    }
}
