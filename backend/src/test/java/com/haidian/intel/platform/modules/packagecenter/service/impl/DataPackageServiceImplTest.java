package com.haidian.intel.platform.modules.packagecenter.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackagePageQueryDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackagePublishDTO;
import com.haidian.intel.platform.modules.packagecenter.entity.DataPackage;
import com.haidian.intel.platform.modules.packagecenter.entity.DataPackageFile;
import com.haidian.intel.platform.modules.packagecenter.mapper.DataPackageFileMapper;
import com.haidian.intel.platform.modules.packagecenter.mapper.DataPackageMapper;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackageDetailVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DataPackagePageVO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for data package management service.
 */
@ExtendWith(MockitoExtension.class)
class DataPackageServiceImplTest {

    @Mock
    private DataPackageMapper dataPackageMapper;

    @Mock
    private DataPackageFileMapper dataPackageFileMapper;

    private DataPackageServiceImpl dataPackageService;

    @BeforeEach
    void setUp() {
        dataPackageService = new DataPackageServiceImpl(dataPackageMapper, dataPackageFileMapper);
    }

    @Test
    void shouldPagePackagesWithStatusLabel() {
        Page<DataPackage> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(buildPublishedPackage(8801L)));
        when(dataPackageMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResponse<DataPackagePageVO> response = dataPackageService.pagePackages(new DataPackagePageQueryDTO());

        assertThat(response.getTotal()).isEqualTo(1L);
        assertThat(response.getRecords()).hasSize(1);
        DataPackagePageVO record = response.getRecords().getFirst();
        assertThat(record.getPackageName()).isEqualTo("海淀月报数据包");
        assertThat(record.getPackageType()).isEqualTo("monthly_brief");
        assertThat(record.getPackageTypeLabel()).isEqualTo("月度简报包");
        assertThat(record.getStatus()).isEqualTo("published");
        assertThat(record.getStatusLabel()).isEqualTo("已发布");
        assertThat(record.getPublishVersion()).isEqualTo(2);
    }

    @Test
    void shouldPublishPackageWithPublishSnapshot() {
        DataPackage draftPackage = buildDraftPackage(8802L);
        DataPackage publishedPackage = buildPublishedPackage(8802L);
        DataPackagePublishDTO publishDTO = new DataPackagePublishDTO();
        publishDTO.setPublishTime(LocalDateTime.of(2026, 4, 8, 10, 0));
        publishDTO.setPublishedByName("情报值班员");
        publishDTO.setPublishNote("Task-09 publish test");

        when(dataPackageMapper.selectById(8802L)).thenReturn(draftPackage, publishedPackage);
        when(dataPackageFileMapper.selectList(any())).thenReturn(List.of(buildPackageFile(9101L, 8802L)));

        DataPackageDetailVO detailVO = dataPackageService.publishPackage(8802L, publishDTO);

        assertThat(detailVO.getId()).isEqualTo(8802L);
        assertThat(detailVO.getStatus()).isEqualTo("published");
        assertThat(detailVO.getStatusLabel()).isEqualTo("已发布");
        assertThat(detailVO.getPublishVersion()).isEqualTo(2);
        assertThat(detailVO.getPublishedByName()).isEqualTo("情报值班员");
        assertThat(detailVO.getPublishNote()).isEqualTo("Task-09 publish test");
    }

    @Test
    void shouldRejectPublishPackageWithoutFiles() {
        when(dataPackageMapper.selectById(8803L)).thenReturn(buildDraftPackage(8803L));
        when(dataPackageFileMapper.selectList(any())).thenReturn(List.of());

        assertThatThrownBy(() -> dataPackageService.publishPackage(8803L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Please maintain package files before publishing");
    }

    private DataPackage buildDraftPackage(Long packageId) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.setId(packageId);
        dataPackage.setPackageName("海淀月报数据包");
        dataPackage.setPackageType("monthly_brief");
        dataPackage.setStatMonth("2026-04");
        dataPackage.setRegionScope("海淀区");
        dataPackage.setFileCount(1);
        dataPackage.setTotalSize(2048L);
        dataPackage.setStatus("draft");
        dataPackage.setPublishVersion(1);
        dataPackage.setDescription("Task-09 package publish test");
        dataPackage.setRemark("remark");
        dataPackage.setCreatedTime(LocalDateTime.of(2026, 4, 8, 9, 0));
        dataPackage.setUpdatedTime(LocalDateTime.of(2026, 4, 8, 9, 30));
        return dataPackage;
    }

    private DataPackage buildPublishedPackage(Long packageId) {
        DataPackage dataPackage = buildDraftPackage(packageId);
        dataPackage.setStatus("published");
        dataPackage.setPublishVersion(2);
        dataPackage.setPublishTime(LocalDateTime.of(2026, 4, 8, 10, 0));
        dataPackage.setPublishedByName("情报值班员");
        dataPackage.setPublishNote("Task-09 publish test");
        return dataPackage;
    }

    private DataPackageFile buildPackageFile(Long fileId, Long packageId) {
        DataPackageFile file = new DataPackageFile();
        file.setId(fileId);
        file.setPackageId(packageId);
        file.setFileName("briefing.zip");
        file.setFilePath("/package/2026-04/briefing.zip");
        file.setFileSize(2048L);
        file.setSortOrder(1);
        return file;
    }
}
