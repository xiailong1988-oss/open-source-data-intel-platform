package com.haidian.intel.platform.modules.collect.controller;

import com.haidian.intel.platform.common.api.ApiResponse;
import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.common.auth.RequireRole;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskCreateDTO;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskPageQueryDTO;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskUpdateDTO;
import com.haidian.intel.platform.modules.collect.service.CollectionTaskService;
import com.haidian.intel.platform.modules.collect.vo.CollectionTaskDetailVO;
import com.haidian.intel.platform.modules.collect.vo.CollectionTaskPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for collection task management APIs.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/collect/tasks")
@Tag(name = "采集任务管理")
@RequireRole("ADMIN")
public class CollectionTaskController {

    private final CollectionTaskService collectionTaskService;

    @GetMapping("/page")
    @Operation(summary = "分页查询采集任务")
    public ApiResponse<PageResponse<CollectionTaskPageVO>> pageTasks(
            @Valid @ParameterObject CollectionTaskPageQueryDTO queryDTO
    ) {
        return ApiResponse.success(collectionTaskService.pageTasks(queryDTO));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "查询采集任务详情")
    public ApiResponse<CollectionTaskDetailVO> getTaskDetail(
            @PathVariable("taskId") @Positive(message = "taskId must be greater than 0") Long taskId
    ) {
        return ApiResponse.success(collectionTaskService.getTaskDetail(taskId));
    }

    @PostMapping
    @Operation(summary = "新增采集任务")
    public ApiResponse<CollectionTaskDetailVO> createTask(
            @Valid @RequestBody CollectionTaskCreateDTO createDTO
    ) {
        return ApiResponse.success(collectionTaskService.createTask(createDTO));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "编辑采集任务")
    public ApiResponse<CollectionTaskDetailVO> updateTask(
            @PathVariable("taskId") @Positive(message = "taskId must be greater than 0") Long taskId,
            @Valid @RequestBody CollectionTaskUpdateDTO updateDTO
    ) {
        return ApiResponse.success(collectionTaskService.updateTask(taskId, updateDTO));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除采集任务")
    public ApiResponse<Void> deleteTask(
            @PathVariable("taskId") @Positive(message = "taskId must be greater than 0") Long taskId
    ) {
        collectionTaskService.deleteTask(taskId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{taskId}/enable")
    @Operation(summary = "启用采集任务")
    public ApiResponse<CollectionTaskDetailVO> enableTask(
            @PathVariable("taskId") @Positive(message = "taskId must be greater than 0") Long taskId
    ) {
        return ApiResponse.success(collectionTaskService.enableTask(taskId));
    }

    @PutMapping("/{taskId}/disable")
    @Operation(summary = "停用采集任务")
    public ApiResponse<CollectionTaskDetailVO> disableTask(
            @PathVariable("taskId") @Positive(message = "taskId must be greater than 0") Long taskId
    ) {
        return ApiResponse.success(collectionTaskService.disableTask(taskId));
    }
}
