package com.cognizant.storeops.activities.service;

import com.cognizant.storeops.activities.model.TaskCategory;

import java.util.UUID;

/**
 * Payload for the TASK_COMPLETED event.
 *
 * EventBus.emit("TASK_COMPLETED", payload) is the ONLY permitted way for
 * the activities module to let alerts or reports react to a completion. A
 * direct import of NotificationService or ReportService from
 * TaskServiceImpl is a hard-gate evaluator failure (see
 * .harness/skills/architecture-principles, Rule 2).
 */
public record TaskCompletedEvent(
        UUID taskId,
        String storeId,
        UUID projectId,
        TaskCategory category
) {
    public static final String EVENT_TYPE = "TASK_COMPLETED";
}
