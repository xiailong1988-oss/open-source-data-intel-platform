package com.haidian.intel.platform.modules.region.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.region.dto.RegionTreeQueryDTO;
import com.haidian.intel.platform.modules.region.entity.Region;
import com.haidian.intel.platform.modules.region.entity.RegionGeo;
import com.haidian.intel.platform.modules.region.mapper.RegionGeoMapper;
import com.haidian.intel.platform.modules.region.mapper.RegionMapper;
import com.haidian.intel.platform.modules.region.service.RegionService;
import com.haidian.intel.platform.modules.region.vo.RegionDetailVO;
import com.haidian.intel.platform.modules.region.vo.RegionGeoVO;
import com.haidian.intel.platform.modules.region.vo.RegionTreeNodeVO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 地区基础服务实现。
 * 当前专注于读取 Flyway 初始化后的地区主数据和边界数据，不扩展到聚合统计逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private static final Comparator<Region> REGION_COMPARATOR = Comparator
            .comparing(Region::getSortNo, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(Region::getId, Comparator.nullsLast(Long::compareTo));

    private final RegionMapper regionMapper;
    private final RegionGeoMapper regionGeoMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<RegionTreeNodeVO> getRegionTree(RegionTreeQueryDTO queryDTO) {
        RegionTreeQueryDTO actualQuery = queryDTO == null ? new RegionTreeQueryDTO() : queryDTO;
        List<Region> regions = regionMapper.selectList(
                Wrappers.<Region>lambdaQuery()
                        .eq(Boolean.TRUE.equals(actualQuery.getEnabledOnly()), Region::getEnabled, 1)
                        .orderByAsc(Region::getSortNo, Region::getId)
        );
        return buildTree(regions);
    }

    @Override
    public RegionDetailVO getRegionDetail(Long regionId) {
        Region region = getRegionOrThrow(regionId);
        Long geoCount = regionGeoMapper.selectCount(
                Wrappers.<RegionGeo>lambdaQuery().eq(RegionGeo::getRegionId, regionId)
        );

        RegionDetailVO detailVO = new RegionDetailVO();
        detailVO.setId(region.getId());
        detailVO.setParentId(region.getParentId());
        detailVO.setRegionCode(region.getRegionCode());
        detailVO.setRegionName(region.getRegionName());
        detailVO.setRegionLevel(region.getRegionLevel());
        detailVO.setRegionType(region.getRegionType());
        detailVO.setCenterLng(region.getCenterLng());
        detailVO.setCenterLat(region.getCenterLat());
        detailVO.setZoomLevel(region.getZoomLevel());
        detailVO.setSortNo(region.getSortNo());
        detailVO.setEnabled(region.getEnabled());
        detailVO.setHasGeoBoundary(geoCount != null && geoCount > 0);
        return detailVO;
    }

    @Override
    public RegionGeoVO getRegionGeo(Long regionId) {
        Region region = getRegionOrThrow(regionId);
        RegionGeo regionGeo = regionGeoMapper.selectOne(
                Wrappers.<RegionGeo>lambdaQuery()
                        .eq(RegionGeo::getRegionId, regionId)
                        .last("LIMIT 1")
        );
        if (regionGeo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地区边界数据不存在");
        }

        RegionGeoVO geoVO = new RegionGeoVO();
        geoVO.setRegionId(region.getId());
        geoVO.setRegionCode(region.getRegionCode());
        geoVO.setRegionName(region.getRegionName());
        geoVO.setGeoJson(parseJson(regionGeo.getGeoJson(), "geoJson"));
        geoVO.setSimplifiedGeoJson(parseJson(regionGeo.getSimplifiedGeoJson(), "simplifiedGeoJson"));
        geoVO.setVersionNo(regionGeo.getVersionNo());
        geoVO.setStatus(regionGeo.getStatus());
        return geoVO;
    }

    /**
     * 将平铺地区列表组装成树结构，并保证同层节点按排序号稳定输出。
     *
     * @param regions 地区列表
     * @return 树形地区结构
     */
    List<RegionTreeNodeVO> buildTree(List<Region> regions) {
        if (regions == null || regions.isEmpty()) {
            return List.of();
        }

        Map<Long, RegionTreeNodeVO> nodeMap = regions.stream()
                .sorted(REGION_COMPARATOR)
                .map(this::toTreeNode)
                .collect(Collectors.toMap(RegionTreeNodeVO::getId, Function.identity(), (left, right) -> left));

        List<RegionTreeNodeVO> rootNodes = new ArrayList<>();
        for (RegionTreeNodeVO node : nodeMap.values()) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L || !nodeMap.containsKey(parentId)) {
                rootNodes.add(node);
                continue;
            }
            nodeMap.get(parentId).getChildren().add(node);
        }

        sortTreeNodes(rootNodes);
        return rootNodes;
    }

    private void sortTreeNodes(List<RegionTreeNodeVO> nodes) {
        nodes.sort(Comparator
                .comparing(RegionTreeNodeVO::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(RegionTreeNodeVO::getId, Comparator.nullsLast(Long::compareTo)));
        for (RegionTreeNodeVO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTreeNodes(node.getChildren());
            }
        }
    }

    private RegionTreeNodeVO toTreeNode(Region region) {
        RegionTreeNodeVO nodeVO = new RegionTreeNodeVO();
        nodeVO.setId(region.getId());
        nodeVO.setParentId(region.getParentId());
        nodeVO.setRegionCode(region.getRegionCode());
        nodeVO.setRegionName(region.getRegionName());
        nodeVO.setRegionLevel(region.getRegionLevel());
        nodeVO.setRegionType(region.getRegionType());
        nodeVO.setCenterLng(region.getCenterLng());
        nodeVO.setCenterLat(region.getCenterLat());
        nodeVO.setZoomLevel(region.getZoomLevel());
        nodeVO.setSortNo(region.getSortNo());
        nodeVO.setEnabled(region.getEnabled());
        return nodeVO;
    }

    private Region getRegionOrThrow(Long regionId) {
        Region region = regionMapper.selectById(regionId);
        if (region == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地区不存在");
        }
        return region;
    }

    private JsonNode parseJson(String json, String fieldName) {
        try {
            return objectMapper.readTree(Objects.requireNonNullElse(json, "{}"));
        } catch (JsonProcessingException exception) {
            log.error("Failed to parse region json field: {}", fieldName, exception);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "地区边界数据格式不正确");
        }
    }
}
