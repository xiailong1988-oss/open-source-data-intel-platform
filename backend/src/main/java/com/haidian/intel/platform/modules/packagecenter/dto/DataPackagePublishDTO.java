package com.haidian.intel.platform.modules.packagecenter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for publishing a data package.
 */
@Getter
@Setter
@Schema(description = "Data package publish DTO")
public class DataPackagePublishDTO {

    @Schema(description = "Publish time, defaults to current time")
    private LocalDateTime publishTime;

    @Size(max = 128, message = "publishedByName length must not exceed 128")
    @Schema(description = "Publisher name")
    private String publishedByName;

    @Size(max = 500, message = "publishNote length must not exceed 500")
    @Schema(description = "Publish note")
    private String publishNote;
}
