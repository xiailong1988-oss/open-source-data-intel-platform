package com.haidian.intel.platform.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.haidian.intel.platform.modules.packagecenter.dto.DataPackageFileSaveDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DataPackagePageQueryDTO;
import com.haidian.intel.platform.modules.packagecenter.dto.DeliveryRecordCreateDTO;
import com.haidian.intel.platform.modules.source.dto.CollectionSourceCreateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Bean validation tests for key request DTOs.
 */
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldRejectInvalidSourceUrl() {
        CollectionSourceCreateDTO dto = buildSourceCreateDTO();
        dto.setSourceUrl("demo-source-address");

        assertThat(extractMessages(validator.validate(dto)))
                .contains("sourceUrl must include a scheme such as https://, jdbc:, mqtt:, or smb://");
    }

    @Test
    void shouldAcceptSchemeBasedSourceUrl() {
        CollectionSourceCreateDTO dto = buildSourceCreateDTO();
        dto.setSourceUrl("jdbc:mysql://demo.local/source_db");

        assertThat(extractMessages(validator.validate(dto))).doesNotContain(
                "sourceUrl must include a scheme such as https://, jdbc:, mqtt:, or smb://"
        );
    }

    @Test
    void shouldRejectInvalidContactPhone() {
        DeliveryRecordCreateDTO dto = buildDeliveryRecordCreateDTO();
        dto.setContactPhone("abc-123");

        assertThat(extractMessages(validator.validate(dto)))
                .contains("contactPhone must be a valid phone number");
    }

    @Test
    void shouldAcceptBlankContactPhone() {
        DeliveryRecordCreateDTO dto = buildDeliveryRecordCreateDTO();
        dto.setContactPhone("");

        assertThat(extractMessages(validator.validate(dto)))
                .doesNotContain("contactPhone must be a valid phone number");
    }

    @Test
    void shouldRejectInvalidDownloadUrl() {
        DataPackageFileSaveDTO dto = buildDataPackageFileSaveDTO();
        dto.setDownloadUrl("download-local-path");

        assertThat(extractMessages(validator.validate(dto)))
                .contains("downloadUrl must include a scheme such as https:// or smb://");
    }

    @Test
    void shouldAcceptBlankDownloadUrl() {
        DataPackageFileSaveDTO dto = buildDataPackageFileSaveDTO();
        dto.setDownloadUrl("");

        assertThat(extractMessages(validator.validate(dto)))
                .doesNotContain("downloadUrl must include a scheme such as https:// or smb://");
    }

    @Test
    void shouldRejectInvalidStatMonthInPageQuery() {
        DataPackagePageQueryDTO dto = new DataPackagePageQueryDTO();
        dto.setStatMonth("2026/04");

        assertThat(extractMessages(validator.validate(dto)))
                .contains("statMonth must match yyyy-MM");
    }

    private CollectionSourceCreateDTO buildSourceCreateDTO() {
        CollectionSourceCreateDTO dto = new CollectionSourceCreateDTO();
        dto.setSourceName("测试数据源");
        dto.setSourceCategory("gov_system");
        dto.setSourceUrl("https://demo.local/source");
        dto.setAccessType("api");
        dto.setEnabled(1);
        dto.setStatus("running");
        dto.setRegionScope("海淀区");
        dto.setOwnerName("联调人员");
        dto.setLatestCollectTime(LocalDateTime.of(2026, 4, 9, 10, 0));
        dto.setDescription("validation test");
        dto.setRemark("remark");
        return dto;
    }

    private DeliveryRecordCreateDTO buildDeliveryRecordCreateDTO() {
        DeliveryRecordCreateDTO dto = new DeliveryRecordCreateDTO();
        dto.setDeliveryType("data_package");
        dto.setRelatedId(11001L);
        dto.setReceiverName("联调人员");
        dto.setReceiverOrg("联调组");
        dto.setContactPhone("13800000000");
        dto.setDeliveryTime(LocalDateTime.of(2026, 4, 9, 10, 30));
        dto.setDeliveryMethod("email");
        dto.setDescription("validation test");
        dto.setRemark("remark");
        return dto;
    }

    private DataPackageFileSaveDTO buildDataPackageFileSaveDTO() {
        DataPackageFileSaveDTO dto = new DataPackageFileSaveDTO();
        dto.setFileName("demo.xlsx");
        dto.setFilePath("/packages/demo/demo.xlsx");
        dto.setFileSize(1024L);
        dto.setFileType("xlsx");
        dto.setChecksum("checksum-demo");
        dto.setStorageType("LOCAL");
        dto.setStorageBucket("");
        dto.setStorageObjectKey("demo/demo.xlsx");
        dto.setDownloadUrl("https://download.local/demo.xlsx");
        dto.setSortOrder(1);
        dto.setRemark("remark");
        return dto;
    }

    private Set<String> extractMessages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}
