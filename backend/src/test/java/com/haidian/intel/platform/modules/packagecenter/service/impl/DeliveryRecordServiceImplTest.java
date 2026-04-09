package com.haidian.intel.platform.modules.packagecenter.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.exception.BusinessException;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordCreateDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordPageQueryDTO;
import com.haidian.intel.platform.modules.packagecenter.entity.DataPackage;
import com.haidian.intel.platform.modules.packagecenter.entity.DeliveryRecord;
import com.haidian.intel.platform.modules.packagecenter.mapper.DataPackageMapper;
import com.haidian.intel.platform.modules.packagecenter.mapper.DeliveryRecordMapper;
import com.haidian.intel.platform.modules.packagecenter.vo.DeliveryRecordDetailVO;
import com.haidian.intel.platform.modules.packagecenter.vo.DeliveryRecordPageVO;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for delivery record management service.
 */
@ExtendWith(MockitoExtension.class)
class DeliveryRecordServiceImplTest {

    @Mock
    private DeliveryRecordMapper deliveryRecordMapper;

    @Mock
    private DataPackageMapper dataPackageMapper;

    private DeliveryRecordServiceImpl deliveryRecordService;

    @BeforeEach
    void setUp() {
        deliveryRecordService = new DeliveryRecordServiceImpl(deliveryRecordMapper, dataPackageMapper);
    }

    @Test
    void shouldPageDeliveryRecordsWithLabels() {
        Page<DeliveryRecord> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(buildDeliveryRecord(9901L)));
        when(deliveryRecordMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResponse<DeliveryRecordPageVO> response = deliveryRecordService.pageRecords(
                new DeliveryRecordPageQueryDTO()
        );

        assertThat(response.getTotal()).isEqualTo(1L);
        assertThat(response.getRecords()).hasSize(1);
        DeliveryRecordPageVO record = response.getRecords().getFirst();
        assertThat(record.getRelatedName()).isEqualTo("海淀月报数据包");
        assertThat(record.getDeliveryTypeLabel()).isEqualTo("数据包");
        assertThat(record.getDeliveryMethodLabel()).isEqualTo("在线下载");
    }

    @Test
    void shouldCreateDeliveryRecordFromPublishedPackage() {
        DeliveryRecordCreateDTO createDTO = new DeliveryRecordCreateDTO();
        createDTO.setDeliveryType("data_package");
        createDTO.setRelatedId(8802L);
        createDTO.setReceiverName("张三");
        createDTO.setReceiverOrg("海淀区指挥中心");
        createDTO.setContactPhone("13800000000");
        createDTO.setDeliveryTime(LocalDateTime.of(2026, 4, 8, 11, 0));
        createDTO.setDeliveryMethod("online_download");
        createDTO.setDescription("Task-09 delivery create test");
        createDTO.setRemark("remark");

        when(dataPackageMapper.selectById(8802L)).thenReturn(buildPublishedPackage(8802L));
        when(deliveryRecordMapper.insert(any(DeliveryRecord.class))).thenAnswer(invocation -> {
            DeliveryRecord record = invocation.getArgument(0);
            record.setId(9902L);
            return 1;
        });
        when(deliveryRecordMapper.selectById(9902L)).thenReturn(buildDeliveryRecord(9902L));

        DeliveryRecordDetailVO detailVO = deliveryRecordService.createRecord(createDTO);

        assertThat(detailVO.getId()).isEqualTo(9902L);
        assertThat(detailVO.getRelatedName()).isEqualTo("海淀月报数据包");
        assertThat(detailVO.getRelatedVersion()).isEqualTo(2);
        assertThat(detailVO.getReceiverName()).isEqualTo("张三");
        assertThat(detailVO.getDeliveryMethodLabel()).isEqualTo("在线下载");
    }

    @Test
    void shouldRejectCreateRecordWhenPackageIsDraft() {
        DeliveryRecordCreateDTO createDTO = new DeliveryRecordCreateDTO();
        createDTO.setDeliveryType("data_package");
        createDTO.setRelatedId(8803L);
        createDTO.setReceiverName("张三");
        createDTO.setDeliveryTime(LocalDateTime.of(2026, 4, 8, 11, 0));
        createDTO.setDeliveryMethod("online_download");

        when(dataPackageMapper.selectById(8803L)).thenReturn(buildDraftPackage(8803L));

        assertThatThrownBy(() -> deliveryRecordService.createRecord(createDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only published data packages can be delivered");
    }

    private DeliveryRecord buildDeliveryRecord(Long recordId) {
        DeliveryRecord record = new DeliveryRecord();
        record.setId(recordId);
        record.setDeliveryType("data_package");
        record.setRelatedId(8802L);
        record.setRelatedName("海淀月报数据包");
        record.setRelatedVersion(2);
        record.setReceiverName("张三");
        record.setReceiverOrg("海淀区指挥中心");
        record.setContactPhone("13800000000");
        record.setDeliveryTime(LocalDateTime.of(2026, 4, 8, 11, 0));
        record.setDeliveryMethod("online_download");
        record.setDescription("Task-09 delivery create test");
        record.setRemark("remark");
        record.setCreatedTime(LocalDateTime.of(2026, 4, 8, 11, 0));
        record.setUpdatedTime(LocalDateTime.of(2026, 4, 8, 11, 0));
        return record;
    }

    private DataPackage buildPublishedPackage(Long packageId) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.setId(packageId);
        dataPackage.setPackageName("海淀月报数据包");
        dataPackage.setStatus("published");
        dataPackage.setPublishVersion(2);
        return dataPackage;
    }

    private DataPackage buildDraftPackage(Long packageId) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.setId(packageId);
        dataPackage.setPackageName("海淀月报数据包");
        dataPackage.setStatus("draft");
        dataPackage.setPublishVersion(1);
        return dataPackage;
    }
}
