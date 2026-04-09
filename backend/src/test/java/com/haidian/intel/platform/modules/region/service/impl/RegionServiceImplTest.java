package com.haidian.intel.platform.modules.region.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haidian.intel.platform.modules.region.dto.RegionTreeQueryDTO;
import com.haidian.intel.platform.modules.region.entity.Region;
import com.haidian.intel.platform.modules.region.entity.RegionGeo;
import com.haidian.intel.platform.modules.region.mapper.RegionGeoMapper;
import com.haidian.intel.platform.modules.region.mapper.RegionMapper;
import com.haidian.intel.platform.modules.region.vo.RegionGeoVO;
import com.haidian.intel.platform.modules.region.vo.RegionTreeNodeVO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 地区基础服务单元测试。
 * 这里重点验证地区树组装排序和 GeoJSON 解析，避免基础联调接口行为回退。
 */
@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {

    @Mock
    private RegionMapper regionMapper;

    @Mock
    private RegionGeoMapper regionGeoMapper;

    private RegionServiceImpl regionService;

    @BeforeEach
    void setUp() {
        regionService = new RegionServiceImpl(regionMapper, regionGeoMapper, new ObjectMapper());
    }

    @Test
    void shouldBuildSortedRegionTree() {
        when(regionMapper.selectList(any())).thenReturn(List.of(
                buildRegion(3003L, 3001L, "SDJD", "上地街道", 2),
                buildRegion(3001L, 0L, "HDQ", "海淀区", 1),
                buildRegion(3002L, 3001L, "XBWZ", "西北旺镇", 1)
        ));

        List<RegionTreeNodeVO> tree = regionService.getRegionTree(new RegionTreeQueryDTO());

        assertThat(tree).hasSize(1);
        assertThat(tree.getFirst().getRegionName()).isEqualTo("海淀区");
        assertThat(tree.getFirst().getChildren())
                .extracting(RegionTreeNodeVO::getRegionName)
                .containsExactly("西北旺镇", "上地街道");
    }

    @Test
    void shouldReturnParsedRegionGeo() {
        Region region = buildRegion(3001L, 0L, "HDQ", "海淀区", 1);
        RegionGeo regionGeo = new RegionGeo();
        regionGeo.setRegionId(3001L);
        regionGeo.setVersionNo(1);
        regionGeo.setStatus(1);
        regionGeo.setGeoJson("{\"type\":\"FeatureCollection\",\"features\":[]}");
        regionGeo.setSimplifiedGeoJson("{\"type\":\"FeatureCollection\",\"features\":[]}");

        when(regionMapper.selectById(3001L)).thenReturn(region);
        when(regionGeoMapper.selectOne(any())).thenReturn(regionGeo);

        RegionGeoVO geoVO = regionService.getRegionGeo(3001L);

        assertThat(geoVO.getRegionCode()).isEqualTo("HDQ");
        assertThat(geoVO.getGeoJson().path("type").asText()).isEqualTo("FeatureCollection");
        assertThat(geoVO.getSimplifiedGeoJson().path("features").size()).isZero();
    }

    private Region buildRegion(Long id, Long parentId, String code, String name, Integer sortNo) {
        Region region = new Region();
        region.setId(id);
        region.setParentId(parentId);
        region.setRegionCode(code);
        region.setRegionName(name);
        region.setRegionLevel(parentId == 0L ? 1 : 2);
        region.setRegionType(parentId == 0L ? "district" : "street");
        region.setCenterLng(new BigDecimal("116.300000"));
        region.setCenterLat(new BigDecimal("39.900000"));
        region.setZoomLevel(12);
        region.setSortNo(sortNo);
        region.setEnabled(1);
        return region;
    }
}
