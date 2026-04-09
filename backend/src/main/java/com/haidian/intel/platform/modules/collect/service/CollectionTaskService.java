package com.haidian.intel.platform.modules.collect.service;

import com.haidian.intel.platform.common.api.PageResponse;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskCreateDTO;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskPageQueryDTO;
import com.haidian.intel.platform.modules.collect.dto.CollectionTaskUpdateDTO;
import com.haidian.intel.platform.modules.collect.vo.CollectionTaskDetailVO;
import com.haidian.intel.platform.modules.collect.vo.CollectionTaskPageVO;

/**
 * Service for collection task management.
 */
public interface CollectionTaskService {

    /**
     * Page tasks for admin management.
     *
     * @param queryDTO query parameters
     * @return paged task list
     */
    PageResponse<CollectionTaskPageVO> pageTasks(CollectionTaskPageQueryDTO queryDTO);

    /**
     * Get task detail.
     *
     * @param taskId task ID
     * @return task detail
     */
    CollectionTaskDetailVO getTaskDetail(Long taskId);

    /**
     * Create a task.
     *
     * @param createDTO create data
     * @return created task detail
     */
    CollectionTaskDetailVO createTask(CollectionTaskCreateDTO createDTO);

    /**
     * Update a task.
     *
     * @param taskId task ID
     * @param updateDTO update data
     * @return updated task detail
     */
    CollectionTaskDetailVO updateTask(Long taskId, CollectionTaskUpdateDTO updateDTO);

    /**
     * Delete a task.
     *
     * @param taskId task ID
     */
    void deleteTask(Long taskId);

    /**
     * Enable a task.
     *
     * @param taskId task ID
     * @return task detail after enable
     */
    CollectionTaskDetailVO enableTask(Long taskId);

    /**
     * Disable a task.
     *
     * @param taskId task ID
     * @return task detail after disable
     */
    CollectionTaskDetailVO disableTask(Long taskId);
}
