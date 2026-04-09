package com.haidian.intel.platform.modules.intel.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.intel.dto.IntelCreateDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelPageQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelStreamQueryDTO;
import com.haidian.intel.platform.modules.intel.dto.IntelTickerQueryDTO;
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
import com.haidian.intel.platform.modules.intel.vo.IntelDetailVO;
import com.haidian.intel.platform.modules.intel.vo.IntelPageVO;
import com.haidian.intel.platform.modules.intel.vo.IntelStreamVO;
import com.haidian.intel.platform.modules.intel.vo.IntelTickerVO;
import com.haidian.intel.platform.modules.region.entity.Region;
import com.haidian.intel.platform.modules.region.mapper.RegionMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 情报核心服务单元测试。
 * 覆盖后台分页、首页快讯条、首页主情报流和写入校验。
 */
@ExtendWith(MockitoExtension.class)
class IntelServiceImplTest {

    @Mock
    private IntelItemMapper intelItemMapper;

    @Mock
    private IntelSourceMapper intelSourceMapper;

    @Mock
    private IntelTagMapper intelTagMapper;

    @Mock
    private IntelItemTagMapper intelItemTagMapper;

    @Mock
    private EventTypeMapper eventTypeMapper;

    @Mock
    private RegionMapper regionMapper;

    private IntelServiceImpl intelService;

    @BeforeEach
    void setUp() {
        intelService = new IntelServiceImpl(
                intelItemMapper,
                intelSourceMapper,
                intelTagMapper,
                intelItemTagMapper,
                eventTypeMapper,
                regionMapper,
                new ObjectMapper()
        );
    }

    @Test
    void shouldReturnPagedIntelWithRelations() {
        IntelItem intelItem = buildIntelItem(9001L);
        Page<IntelItem> page = new Page<>(1, 20);
        page.setTotal(1);
        page.setRecords(List.of(intelItem));

        when(intelItemMapper.selectPage(any(Page.class), any())).thenReturn(page);
        mockRelationQueries(9001L);

        IntelPageQueryDTO queryDTO = new IntelPageQueryDTO();
        queryDTO.setKeyword(" 校园 ");
        PageResponse<IntelPageVO> pageResponse = intelService.pageIntel(queryDTO);

        assertThat(pageResponse.getTotal()).isEqualTo(1L);
        assertThat(pageResponse.getRecords()).hasSize(1);

        IntelPageVO record = pageResponse.getRecords().getFirst();
        assertThat(record.getTitle()).isEqualTo("校园周边群体活动");
        assertThat(record.getRegionName()).isEqualTo("海淀区");
        assertThat(record.getEventTypeName()).isEqualTo("群体活动");
        assertThat(record.getSourceName()).isEqualTo("人工录入");
        assertThat(record.getTags()).extracting(tag -> tag.getTagName()).containsExactly("校园安全");
    }

    @Test
    void shouldReturnTickerListForHomepage() {
        when(intelItemMapper.selectList(any())).thenReturn(List.of(buildIntelItem(9001L)));
        mockRelationQueries(9001L);

        IntelTickerQueryDTO queryDTO = new IntelTickerQueryDTO();
        queryDTO.setLimit(5);

        List<IntelTickerVO> tickerList = intelService.listIntelTicker(queryDTO);

        assertThat(tickerList).hasSize(1);
        IntelTickerVO tickerVO = tickerList.getFirst();
        assertThat(tickerVO.getDetailId()).isEqualTo(9001L);
        assertThat(tickerVO.getTitle()).isEqualTo("校园周边群体活动");
        assertThat(tickerVO.getRegion()).isEqualTo("海淀区");
        assertThat(tickerVO.getType()).isEqualTo("群体活动");
        assertThat(tickerVO.getSource()).isEqualTo("人工录入");
        assertThat(tickerVO.getSummary()).isEqualTo("接到街道反馈，校园周边出现聚集活动。");
        assertThat(tickerVO.getTags()).containsExactly("校园安全");
    }

    @Test
    void shouldReturnStreamPageForHomepage() {
        IntelItem intelItem = buildIntelItem(9001L);
        Page<IntelItem> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(intelItem));

        when(intelItemMapper.selectPage(any(Page.class), any())).thenReturn(page);
        mockRelationQueries(9001L);

        IntelStreamQueryDTO queryDTO = new IntelStreamQueryDTO();
        PageResponse<IntelStreamVO> pageResponse = intelService.pageIntelStream(queryDTO);

        assertThat(pageResponse.getTotal()).isEqualTo(1L);
        assertThat(pageResponse.getRecords()).hasSize(1);

        IntelStreamVO streamVO = pageResponse.getRecords().getFirst();
        assertThat(streamVO.getDetailId()).isEqualTo(9001L);
        assertThat(streamVO.getTime()).isEqualTo(LocalDateTime.of(2026, 4, 7, 10, 0));
        assertThat(streamVO.getRegion()).isEqualTo("海淀区");
        assertThat(streamVO.getType()).isEqualTo("群体活动");
        assertThat(streamVO.getSource()).isEqualTo("人工录入");
        assertThat(streamVO.getTags()).containsExactly("校园安全");
    }

    @Test
    void shouldReturnEmptyStreamPageWhenNoData() {
        Page<IntelItem> page = new Page<>(1, 10);
        page.setTotal(0);
        page.setRecords(List.of());
        when(intelItemMapper.selectPage(any(Page.class), any())).thenReturn(page);

        IntelStreamQueryDTO queryDTO = new IntelStreamQueryDTO();
        PageResponse<IntelStreamVO> pageResponse = intelService.pageIntelStream(queryDTO);

        assertThat(pageResponse.getTotal()).isZero();
        assertThat(pageResponse.getRecords()).isEmpty();
        assertThat(pageResponse.getPageNum()).isEqualTo(1L);
        assertThat(pageResponse.getPageSize()).isEqualTo(10L);
    }

    @Test
    void shouldCreateIntelAndSaveDistinctTagRelations() {
        IntelCreateDTO createDTO = buildCreateDTO();

        when(regionMapper.selectOne(any())).thenReturn(buildRegion());
        when(eventTypeMapper.selectOne(any())).thenReturn(buildEventType());
        when(intelSourceMapper.selectOne(any())).thenReturn(buildSource());
        when(intelTagMapper.selectCount(any())).thenReturn(2L);
        when(intelItemMapper.insert(any(IntelItem.class))).thenAnswer(invocation -> {
            IntelItem item = invocation.getArgument(0);
            item.setId(9001L);
            return 1;
        });
        when(intelItemMapper.selectById(9001L)).thenReturn(buildIntelItem(9001L));
        when(regionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildRegion()));
        when(eventTypeMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildEventType()));
        when(intelSourceMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildSource()));
        when(intelItemTagMapper.selectList(any())).thenReturn(List.of(
                buildRelation(9001L, 7002L),
                buildRelation(9001L, 7001L)
        ));
        when(intelTagMapper.selectBatchIds(anyCollection())).thenReturn(List.of(
                buildTag(7002L, "群体活动", "theme", 2),
                buildTag(7001L, "校园安全", "theme", 1)
        ));

        IntelDetailVO detailVO = intelService.createIntel(createDTO);

        ArgumentCaptor<IntelItemTag> relationCaptor = ArgumentCaptor.forClass(IntelItemTag.class);
        verify(intelItemTagMapper, times(2)).insert(relationCaptor.capture());

        assertThat(relationCaptor.getAllValues())
                .extracting(IntelItemTag::getTagId)
                .containsExactly(7002L, 7001L);
        assertThat(detailVO.getId()).isEqualTo(9001L);
        assertThat(detailVO.getSourceName()).isEqualTo("人工录入");
        assertThat(detailVO.getTags())
                .extracting(tag -> tag.getTagName())
                .containsExactly("校园安全", "群体活动");
        assertThat(detailVO.getExtJson().path("level").asText()).isEqualTo("high");
    }

    @Test
    void shouldRejectDisabledSourceWhenCreatingIntel() {
        IntelCreateDTO createDTO = buildCreateDTO();

        when(regionMapper.selectOne(any())).thenReturn(buildRegion());
        when(eventTypeMapper.selectOne(any())).thenReturn(buildEventType());
        when(intelSourceMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> intelService.createIntel(createDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("来源不存在或已停用");
    }

    private void mockRelationQueries(Long intelId) {
        when(regionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildRegion()));
        when(eventTypeMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildEventType()));
        when(intelSourceMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildSource()));
        when(intelItemTagMapper.selectList(any())).thenReturn(List.of(buildRelation(intelId, 7001L)));
        when(intelTagMapper.selectBatchIds(anyCollection())).thenReturn(List.of(buildTag(7001L, "校园安全", "theme", 1)));
    }

    private IntelCreateDTO buildCreateDTO() {
        IntelCreateDTO createDTO = new IntelCreateDTO();
        createDTO.setTitle("校园周边群体活动");
        createDTO.setSummary("接到街道反馈，校园周边出现聚集活动。");
        createDTO.setContent("现场已安排人员处置，当前秩序整体可控。");
        createDTO.setRegionId(3001L);
        createDTO.setEventTypeId(4002L);
        createDTO.setSourceId(6001L);
        createDTO.setImportanceLevel(3);
        createDTO.setHeatScore(85);
        createDTO.setFocusFlag(1);
        createDTO.setRiskLevel(2);
        createDTO.setOccurTime(LocalDateTime.of(2026, 4, 7, 9, 30));
        createDTO.setPublishTime(LocalDateTime.of(2026, 4, 7, 10, 0));
        createDTO.setLng(new BigDecimal("116.305000"));
        createDTO.setLat(new BigDecimal("39.985000"));
        createDTO.setAddress("海淀区学院路某校园周边");
        createDTO.setStatus(1);
        createDTO.setReviewStatus(1);
        createDTO.setDataOrigin("manual");
        createDTO.setExtJson(new ObjectMapper().createObjectNode().put("level", "high"));
        createDTO.setRemark("Task-05 单元测试数据");
        createDTO.setTagIds(List.of(7002L, 7001L, 7002L));
        return createDTO;
    }

    private IntelItem buildIntelItem(Long intelId) {
        IntelItem intelItem = new IntelItem();
        intelItem.setId(intelId);
        intelItem.setTitle("校园周边群体活动");
        intelItem.setSummary("接到街道反馈，校园周边出现聚集活动。");
        intelItem.setContent("现场已安排人员处置，当前秩序整体可控。");
        intelItem.setRegionId(3001L);
        intelItem.setEventTypeId(4002L);
        intelItem.setSourceId(6001L);
        intelItem.setImportanceLevel(3);
        intelItem.setHeatScore(85);
        intelItem.setFocusFlag(1);
        intelItem.setRiskLevel(2);
        intelItem.setOccurTime(LocalDateTime.of(2026, 4, 7, 9, 30));
        intelItem.setPublishTime(LocalDateTime.of(2026, 4, 7, 10, 0));
        intelItem.setLng(new BigDecimal("116.305000"));
        intelItem.setLat(new BigDecimal("39.985000"));
        intelItem.setAddress("海淀区学院路某校园周边");
        intelItem.setStatus(1);
        intelItem.setReviewStatus(1);
        intelItem.setDataOrigin("manual");
        intelItem.setExtJson("{\"level\":\"high\"}");
        intelItem.setRemark("Task-05 单元测试数据");
        intelItem.setCreatedTime(LocalDateTime.of(2026, 4, 7, 10, 0));
        intelItem.setUpdatedTime(LocalDateTime.of(2026, 4, 7, 10, 5));
        return intelItem;
    }

    private Region buildRegion() {
        Region region = new Region();
        region.setId(3001L);
        region.setRegionCode("HDQ");
        region.setRegionName("海淀区");
        region.setEnabled(1);
        return region;
    }

    private EventType buildEventType() {
        EventType eventType = new EventType();
        eventType.setId(4002L);
        eventType.setEventCode("group_activity");
        eventType.setEventName("群体活动");
        eventType.setEventCategory("event");
        eventType.setColorCode("#FF7A45");
        eventType.setIconCode("warning-circle");
        eventType.setEnabled(1);
        return eventType;
    }

    private IntelSource buildSource() {
        IntelSource source = new IntelSource();
        source.setId(6001L);
        source.setSourceName("人工录入");
        source.setSourceType("manual");
        source.setSourceUrl("");
        source.setCredibilityLevel(5);
        source.setEnabled(1);
        return source;
    }

    private IntelTag buildTag(Long tagId, String tagName, String tagType, Integer sortNo) {
        IntelTag tag = new IntelTag();
        tag.setId(tagId);
        tag.setTagName(tagName);
        tag.setTagType(tagType);
        tag.setSortNo(sortNo);
        tag.setEnabled(1);
        return tag;
    }

    private IntelItemTag buildRelation(Long intelId, Long tagId) {
        IntelItemTag relation = new IntelItemTag();
        relation.setIntelItemId(intelId);
        relation.setTagId(tagId);
        return relation;
    }
}
