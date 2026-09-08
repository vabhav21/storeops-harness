package com.cognizant.storeops.reports.service;

import com.cognizant.storeops.activities.model.Task;
import com.cognizant.storeops.activities.model.TaskCategory;
import com.cognizant.storeops.activities.model.TaskPriority;
import com.cognizant.storeops.activities.model.TaskStatus;
import com.cognizant.storeops.activities.service.TaskService;
import com.cognizant.storeops.programmes.model.Project;
import com.cognizant.storeops.programmes.service.ProjectService;
import com.cognizant.storeops.reports.dto.StoreSummaryResponse;
import com.cognizant.storeops.reports.model.Report;
import com.cognizant.storeops.reports.model.ReportStatus;
import com.cognizant.storeops.reports.repository.ReportRepository;
import com.cognizant.storeops.shared.error.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Business-rule tests for the reports ReportService, including the Rule 5
 * guarantee that reports never mutate another module's state.
 */
class ReportServiceImplTest {

    private ReportRepository reportRepository;
    private ProjectService projectService;
    private TaskService taskService;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportRepository = mock(ReportRepository.class);
        projectService = mock(ProjectService.class);
        taskService = mock(TaskService.class);
        reportService = new ReportServiceImpl(reportRepository, projectService, taskService);
        when(reportRepository.save(any(Report.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void generateStoreSummary_givenBlankStoreId_thenThrowsValidationError() {
        assertThatThrownBy(() -> reportService.generateStoreSummary(" "))
                .isInstanceOf(ValidationError.class);
    }

    @Test
    void generateStoreSummary_givenNoTasks_thenCompletionRateIsZeroAndNotNaN() {
        when(projectService.listByStore("store-1")).thenReturn(List.of());
        when(taskService.listByStore("store-1")).thenReturn(List.of());

        StoreSummaryResponse summary = reportService.generateStoreSummary("store-1");

        assertThat(summary.taskCount()).isZero();
        assertThat(summary.completionRate()).isEqualTo(0.0);
        assertThat(summary.overdueByCategory()).isEmpty();
    }

    @Test
    void generateStoreSummary_givenMixedTasks_thenComputesCompletionRateAndProgrammeCount() {
        when(projectService.listByStore("store-2")).thenReturn(List.of(
                new Project("Spring reset", "store-2", "region-1"),
                new Project("Chiller audit", "store-2", "region-1")));
        when(taskService.listByStore("store-2")).thenReturn(List.of(
                taskWith(TaskCategory.PLANOGRAM, TaskStatus.DONE, null),
                taskWith(TaskCategory.PLANOGRAM, TaskStatus.TODO, null),
                taskWith(TaskCategory.AUDIT, TaskStatus.DONE, null),
                taskWith(TaskCategory.AUDIT, TaskStatus.IN_PROGRESS, null)));

        StoreSummaryResponse summary = reportService.generateStoreSummary("store-2");

        assertThat(summary.storeId()).isEqualTo("store-2");
        assertThat(summary.programmeCount()).isEqualTo(2);
        assertThat(summary.taskCount()).isEqualTo(4);
        assertThat(summary.completedCount()).isEqualTo(2);
        assertThat(summary.completionRate()).isEqualTo(0.5);
    }

    @Test
    void generateStoreSummary_givenOverdueTasks_thenGroupsOverdueCountsByCategoryExcludingDone() {
        Instant past = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant future = Instant.now().plus(2, ChronoUnit.DAYS);
        when(projectService.listByStore("store-3")).thenReturn(List.of());
        when(taskService.listByStore("store-3")).thenReturn(List.of(
                taskWith(TaskCategory.RESTOCKING, TaskStatus.TODO, past),
                taskWith(TaskCategory.RESTOCKING, TaskStatus.BLOCKED, past),
                taskWith(TaskCategory.COMPLIANCE, TaskStatus.IN_PROGRESS, past),
                taskWith(TaskCategory.COMPLIANCE, TaskStatus.DONE, past),
                taskWith(TaskCategory.AUDIT, TaskStatus.TODO, future),
                taskWith(TaskCategory.AUDIT, TaskStatus.TODO, null)));

        StoreSummaryResponse summary = reportService.generateStoreSummary("store-3");

        assertThat(summary.overdueByCategory())
                .containsEntry(TaskCategory.RESTOCKING, 2L)
                .containsEntry(TaskCategory.COMPLIANCE, 1L)
                .doesNotContainKey(TaskCategory.AUDIT);
    }

    @Test
    void generateStoreSummary_givenValidStoreId_thenPersistsReadyReportWithCounts() {
        when(projectService.listByStore("store-4")).thenReturn(List.of());
        when(taskService.listByStore("store-4")).thenReturn(List.of(
                taskWith(TaskCategory.GENERAL, TaskStatus.DONE, null),
                taskWith(TaskCategory.GENERAL, TaskStatus.TODO, null)));

        StoreSummaryResponse summary = reportService.generateStoreSummary("store-4");

        assertThat(summary.generatedAt()).isNotNull();

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ReportStatus.READY);
        assertThat(captor.getValue().getScopeId()).isEqualTo("store-4");
        assertThat(captor.getValue().getTaskCount()).isEqualTo(2);
        assertThat(captor.getValue().getCompletedCount()).isEqualTo(1);
    }

    /**
     * Rule 5: reports is read-only. It may call the owning modules' service
     * layers, but never a method that mutates their state. Asserting the
     * absence of writes is the only way this rule can regress silently.
     */
    @Test
    void generateStoreSummary_thenNeverMutatesActivitiesOrProgrammesState() {
        when(projectService.listByStore("store-5")).thenReturn(List.of());
        when(taskService.listByStore("store-5")).thenReturn(List.of(
                taskWith(TaskCategory.GENERAL, TaskStatus.TODO, null)));

        reportService.generateStoreSummary("store-5");

        verify(taskService, never()).createTask(any());
        verify(taskService, never()).updateStatus(any(), any());
        verify(projectService, never()).createProject(any(), any(), any());
        verify(projectService, never()).applyTemplate(any(), any());
        verify(projectService, never()).addMember(any(), any(), any());
        verify(projectService, never()).closeProject(any());
        assertThat(mockingDetails(reportRepository).getInvocations()).hasSize(1);
    }

    private static Task taskWith(TaskCategory category, TaskStatus status, Instant dueAt) {
        Task task = new Task("Task " + UUID.randomUUID(), "store-x", category, TaskPriority.MEDIUM);
        task.setStatus(status);
        task.setDueAt(dueAt);
        return task;
    }
}
