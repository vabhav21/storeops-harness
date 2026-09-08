package com.cognizant.storeops.reports.service;

import com.cognizant.storeops.activities.model.Task;
import com.cognizant.storeops.activities.model.TaskCategory;
import com.cognizant.storeops.activities.model.TaskStatus;
import com.cognizant.storeops.activities.service.TaskService;
import com.cognizant.storeops.programmes.service.ProjectService;
import com.cognizant.storeops.reports.dto.StoreSummaryResponse;
import com.cognizant.storeops.reports.model.Report;
import com.cognizant.storeops.reports.model.ReportStatus;
import com.cognizant.storeops.reports.model.ReportType;
import com.cognizant.storeops.reports.repository.ReportRepository;
import com.cognizant.storeops.shared.error.ValidationError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates store performance from the activities and programmes modules.
 *
 * Note what is injected: TaskService and ProjectService — the owning
 * modules' SERVICE layers — not TaskRepository or ProjectRepository. That
 * is Rule 1 (module boundary) in practice. Note also what is NOT called:
 * no method on either service that mutates state. That is Rule 5.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ProjectService projectService;
    private final TaskService taskService;

    public ReportServiceImpl(ReportRepository reportRepository, ProjectService projectService, TaskService taskService) {
        this.reportRepository = reportRepository;
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @Override
    @Transactional
    public StoreSummaryResponse generateStoreSummary(String storeId) {
        if (storeId == null || storeId.isBlank()) {
            throw new ValidationError("storeId is required to generate a store summary");
        }

        int programmeCount = projectService.listByStore(storeId).size();
        List<Task> tasks = taskService.listByStore(storeId);

        long completedCount = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();
        Map<TaskCategory, Long> overdueByCategory = tasks.stream()
                .filter(ReportServiceImpl::isOverdue)
                .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));

        Report report = new Report(ReportType.STORE_SUMMARY, storeId);
        report.setTaskCount(tasks.size());
        report.setCompletedCount((int) completedCount);
        report.setStatus(ReportStatus.READY);
        Report saved = reportRepository.save(report);

        double completionRate = tasks.isEmpty() ? 0.0 : (double) completedCount / tasks.size();

        return new StoreSummaryResponse(
                saved.getId(),
                storeId,
                programmeCount,
                tasks.size(),
                (int) completedCount,
                completionRate,
                overdueByCategory,
                saved.getGeneratedAt()
        );
    }

    private static boolean isOverdue(Task task) {
        return task.getDueAt() != null
                && task.getStatus() != TaskStatus.DONE
                && task.getDueAt().isBefore(Instant.now());
    }
}
