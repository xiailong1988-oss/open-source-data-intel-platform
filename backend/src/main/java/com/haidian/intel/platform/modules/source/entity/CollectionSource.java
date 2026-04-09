package com.haidian.intel.platform.modules.source.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.haidian.intel.platform.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Collection source entity for admin source management.
 */
@Getter
@Setter
@TableName("biz_collection_source")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Collection source entity")
public class CollectionSource extends BaseEntity {

    @Schema(description = "Source name")
    private String sourceName;

    @Schema(description = "Source category code")
    private String sourceCategory;

    @Schema(description = "Source endpoint or address")
    private String sourceUrl;

    @Schema(description = "Access type code")
    private String accessType;

    @Schema(description = "Enable flag")
    private Integer enabled;

    @Schema(description = "Source status code")
    private String status;

    @Schema(description = "Region scope text")
    private String regionScope;

    @Schema(description = "Owner name")
    private String ownerName;

    @Schema(description = "Latest collect time")
    private LocalDateTime latestCollectTime;

    @Schema(description = "Source description")
    private String description;
}
