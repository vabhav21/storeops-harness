package com.cognizant.storeops.activities.dto;

import com.cognizant.storeops.activities.model.Task;
import com.cognizant.storeops.activities.model.TaskCategory;
import com.cognizant.storeops.activities.model.TaskPriority;
import com.cognizant.storeops.activities.model.TaskStatus;

import java.time.Instant;
import java.util.UUID;

/** Read-model returned by activity routes. Keeps the Task entity out of the HTTP layer. */
public record TaskResponse(
        UUID id,
        String title,
        String storeId,
        UUID projectId,
        String department,
        String assigneeStaffId,
        TaskCategory category,
        TaskPriority priority,
        TaskStatus status,
        Instant dueAt,
        Instant createdAt,
        Instant completedAt
) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStoreId(),
                task.getProjectId(),
                task.getDepartment(),
                task.getAssigneeStaffId(),
                task.getCategory(),
                task.getPriority(),
                task.getStatus(),
                task.getDueAt(),
                task.getCreatedAt(),
                task.getCompletedAt()
        );
    }
}
