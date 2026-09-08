package com.cognizant.storeops.activities.service;

import com.cognizant.storeops.activities.dto.CreateTaskRequest;
import com.cognizant.storeops.activities.model.Task;
import com.cognizant.storeops.activities.model.TaskCategory;
import com.cognizant.storeops.activities.model.TaskPriority;
import com.cognizant.storeops.activities.model.TaskStatus;
import com.cognizant.storeops.activities.repository.TaskRepository;
import com.cognizant.storeops.shared.error.ConflictError;
import com.cognizant.storeops.shared.error.NotFoundError;
import com.cognizant.storeops.shared.error.ValidationError;
import com.cognizant.storeops.shared.events.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Business-rule tests for the activities TaskService. Asserts resulting
 * entity state and emitted events, not just "no exception thrown" — per
 * how-to-test/SKILL.md and failure mode #3.
 */
class TaskServiceImplTest {

    private TaskRepository taskRepository;
    private EventBus eventBus;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        eventBus = mock(EventBus.class);
        taskService = new TaskServiceImpl(taskRepository, eventBus);
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createTask_givenBlankTitle_thenThrowsValidationError() {
        CreateTaskRequest request = new CreateTaskRequest(
                "  ", "store-1", null, "Grocery", null, "PLANOGRAM", "HIGH", null);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ValidationError.class);
    }

    @Test
    void createTask_givenUnknownCategory_thenThrowsValidationError() {
        CreateTaskRequest request = new CreateTaskRequest(
                "Reset endcap 3", "store-1", null, "Grocery", null, "SHELF_WIBBLE", "HIGH", null);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(ValidationError.class);
    }

    @Test
    void createTask_givenNoCategoryOrPriority_thenDefaultsToGeneralMediumTodo() {
        CreateTaskRequest request = new CreateTaskRequest(
                "Sweep aisle 4", "store-1", null, null, null, null, null, null);

        Task task = taskService.createTask(request);

        assertThat(task.getCategory()).isEqualTo(TaskCategory.GENERAL);
        assertThat(task.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void updateStatus_givenUnknownId_thenThrowsNotFoundError() {
        UUID missingId = UUID.randomUUID();
        when(taskRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateStatus(missingId, TaskStatus.DONE))
                .isInstanceOf(NotFoundError.class);
    }

    @Test
    void updateStatus_givenDoneActivity_thenThrowsConflictErrorAndEmitsNothing() {
        Task done = new Task("Reset endcap 3", "store-1", TaskCategory.PLANOGRAM, TaskPriority.HIGH);
        done.setStatus(TaskStatus.DONE);
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.of(done));

        assertThatThrownBy(() -> taskService.updateStatus(id, TaskStatus.IN_PROGRESS))
                .isInstanceOf(ConflictError.class);
        verify(eventBus, never()).emit(any(), any());
    }

    @Test
    void updateStatus_givenTodoActivityMovedToDone_thenStampsCompletedAtAndEmitsTaskCompleted() {
        Task todo = new Task("Reset aisle 7", "store-2", TaskCategory.PLANOGRAM, TaskPriority.MEDIUM);
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.of(todo));

        Task updated = taskService.updateStatus(id, TaskStatus.DONE);

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updated.getCompletedAt()).isNotNull();

        ArgumentCaptor<TaskCompletedEvent> captor = ArgumentCaptor.forClass(TaskCompletedEvent.class);
        verify(eventBus).emit(eq(TaskCompletedEvent.EVENT_TYPE), captor.capture());
        assertThat(captor.getValue().storeId()).isEqualTo("store-2");
        assertThat(captor.getValue().category()).isEqualTo(TaskCategory.PLANOGRAM);
    }

    @Test
    void updateStatus_givenBlockedActivityMovedToInProgress_thenEmitsNoCompletionEvent() {
        Task blocked = new Task("Audit chiller 2", "store-3", TaskCategory.AUDIT, TaskPriority.CRITICAL);
        blocked.setStatus(TaskStatus.BLOCKED);
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.of(blocked));

        Task updated = taskService.updateStatus(id, TaskStatus.IN_PROGRESS);

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updated.getCompletedAt()).isNull();
        verify(eventBus, never()).emit(any(), any());
    }
}
