package com.haidian.intel.platform.modules.region.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haidian.intel.platform.common.enums.ResultCode;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.region.dto.MapLayerQueryDTO;
import com.haidian.intel.platform.modules.region.entity.MapLayer;
import com.haidian.intel.platform.modules.region.mapper.MapLayerMapper;
import com.haidian.intel.platform.modules.region.service.MapLayerService;
import com.haidian.intel.platform.modules.region.vo.MapLayerVO;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 地图图层基础服务实现。
 * 当前只提供列表查询和基础过滤，不扩展缓存和复杂地图业务逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapLayerServiceImpl implements MapLayerService {

    private final MapLayerMapper mapLayerMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<MapLayerVO> getMapLayers(MapLayerQueryDTO queryDTO) {
        MapLayerQueryDTO actualQuery = queryDTO == null ? new MapLayerQueryDTO() : queryDTO;
        List<MapLayer> mapLayers = mapLayerMapper.selectList(
                Wrappers.<MapLayer>lambdaQuery()
                        .eq(Boolean.TRUE.equals(actualQuery.getEnabledOnly()), MapLayer::getEnabled, 1)
                        .eq(StringUtils.hasText(actualQuery.getLayerType()), MapLayer::getLayerType, actualQuery.getLayerType())
                        .orderByAsc(MapLayer::getSortNo, MapLayer::getId)
        );

        return mapLayers.stream()
                .map(this::toMapLayerVO)
                .toList();
    }

    private MapLayerVO toMapLayerVO(MapLayer mapLayer) {
        MapLayerVO layerVO = new MapLayerVO();
        layerVO.setId(mapLayer.getId());
        layerVO.setLayerCode(mapLayer.getLayerCode());
        layerVO.setLayerName(mapLayer.getLayerName());
        layerVO.setLayerType(mapLayer.getLayerType());
        layerVO.setEnabled(mapLayer.getEnabled());
        layerVO.setSortNo(mapLayer.getSortNo());
        layerVO.setStyleConfig(parseJson(mapLayer.getExtJson()));
        return layerVO;
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(Objects.requireNonNullElse(json, "{}"));
        } catch (JsonProcessingException exception) {
            log.error("Failed to parse map layer extJson.", exception);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "地图图层配置格式不正确");
        }
    }
}
