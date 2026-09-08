package com.cognizant.storeops.activities.dto;

import java.time.Instant;
import java.util.UUID;

/** Request body for creating an operational activity. */
public record CreateTaskRequest(
        String title,
        String storeId,
        UUID projectId,
        String department,
        String assigneeStaffId,
        String category,
        String priority,
        Instant dueAt
) {
}
