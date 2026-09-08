package com.cognizant.storeops.reports.dto;

import com.cognizant.storeops.activities.model.TaskCategory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Store performance summary: task completion rate plus overdue counts
 * broken down by TaskCategory, aggregated read-only across the
 * programmes and activities modules.
 */
public record StoreSummaryResponse(
        UUID reportId,
        String storeId,
        int programmeCount,
        int taskCount,
        int completedCount,
        double completionRate,
        Map<TaskCategory, Long> overdueByCategory,
        Instant generatedAt
) {
}
