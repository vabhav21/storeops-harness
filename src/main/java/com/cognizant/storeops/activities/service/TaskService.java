package com.cognizant.storeops.activities.service;

import com.cognizant.storeops.activities.dto.CreateTaskRequest;
import com.cognizant.storeops.activities.model.Task;
import com.cognizant.storeops.activities.model.TaskStatus;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for the activities module.
 *
 * The read methods below are the ONLY entry point other modules may use
 * for activity data — reports calls listByStore/listByProject here rather
 * than autowiring TaskRepository.
 */
public interface TaskService {

    Task createTask(CreateTaskRequest request);

    Task getTask(UUID taskId);

    List<Task> listByStore(String storeId);

    List<Task> listByProject(UUID projectId);

    /**
     * Applies a status transition. DONE is terminal: re-opening a completed
     * activity is a ConflictError, because the shift-handover audit trail
     * assumes a completion timestamp is written once.
     */
    Task updateStatus(UUID taskId, TaskStatus newStatus);
}
