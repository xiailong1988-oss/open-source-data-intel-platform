package com.haidian.intel.platform.modules.source.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Detail VO for source management.
 */
@Getter
@Setter
@Schema(description = "Collection source detail VO")
public class CollectionSourceDetailVO {

    @Schema(description = "Source ID")
    private Long id;

    @Schema(description = "Source name")
    private String sourceName;

    @Schema(description = "Source category code")
    private String sourceCategory;

    @Schema(description = "Source category label")
    private String sourceCategoryLabel;

    @Schema(description = "Access type code")
    private String accessType;

    @Schema(description = "Access type label")
    private String accessTypeLabel;

    @Schema(description = "Enable flag")
    private Integer enabled;

    @Schema(description = "Source status code")
    private String status;

    @Schema(description = "Source status label")
    private String statusLabel;

    @Schema(description = "Region scope text")
    private String regionScope;

    @Schema(description = "Owner name")
    private String ownerName;

    @Schema(description = "Source endpoint or address")
    private String sourceUrl;

    @Schema(description = "Latest collect time")
    private LocalDateTime latestCollectTime;

    @Schema(description = "Source description")
    private String description;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Created time")
    private LocalDateTime createdTime;

    @Schema(description = "Updated time")
    private LocalDateTime updatedTime;
}
