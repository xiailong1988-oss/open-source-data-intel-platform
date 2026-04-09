package com.haidian.intel.platform.common.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Common base entity for business tables.
 */
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @Schema(description = "Primary key ID")
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created by")
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Created time")
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "Updated by")
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "Updated time")
    private LocalDateTime updatedTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "Logical delete flag")
    private Integer deleted;

    @Schema(description = "Remark")
    private String remark;
}
