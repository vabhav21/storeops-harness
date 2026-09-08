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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final EventBus eventBus;

    public TaskServiceImpl(TaskRepository taskRepository, EventBus eventBus) {
        this.taskRepository = taskRepository;
        this.eventBus = eventBus;
    }

    @Override
    @Transactional
    public Task createTask(CreateTaskRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new ValidationError("Activity title must not be blank");
        }
        if (request.storeId() == null || request.storeId().isBlank()) {
            throw new ValidationError("storeId is required to create an activity");
        }

        Task task = new Task(
                request.title(),
                request.storeId(),
                parseCategory(request.category()),
                parsePriority(request.priority()));
        task.setProjectId(request.projectId());
        task.setDepartment(request.department());
        task.setAssigneeStaffId(request.assigneeStaffId());
        task.setDueAt(request.dueAt());

        return taskRepository.save(task);
    }

    @Override
    public Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundError("Task", taskId));
    }

    @Override
    public List<Task> listByStore(String storeId) {
        return taskRepository.findByStoreId(storeId);
    }

    @Override
    public List<Task> listByProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional
    public Task updateStatus(UUID taskId, TaskStatus newStatus) {
        Task task = getTask(taskId);

        if (task.getStatus() == TaskStatus.DONE) {
            throw new ConflictError("Activity " + taskId + " is DONE and cannot be re-opened");
        }
        if (task.getStatus() == newStatus) {
            throw new ConflictError("Activity " + taskId + " is already " + newStatus);
        }

        task.setStatus(newStatus);
        if (newStatus == TaskStatus.DONE) {
            task.setCompletedAt(Instant.now());
        }
        Task saved = taskRepository.save(task);

        if (newStatus == TaskStatus.DONE) {
            // Cross-module side effect: alerts (shift handover) and reports
            // (completion rates) react to this. No direct import of
            // NotificationService or ReportService is permitted here —
            // architecture-principles Rule 2.
            eventBus.emit(
                    TaskCompletedEvent.EVENT_TYPE,
                    new TaskCompletedEvent(
                            saved.getId(),
                            saved.getStoreId(),
                            saved.getProjectId(),
                            saved.getCategory()
                    )
            );
        }

        return saved;
    }

    private TaskCategory parseCategory(String rawCategory) {
        if (rawCategory == null) {
            return TaskCategory.GENERAL;
        }
        try {
            return TaskCategory.valueOf(rawCategory);
        } catch (IllegalArgumentException ex) {
            throw new ValidationError("category must be one of RESTOCKING, PLANOGRAM, AUDIT, COMPLIANCE, GENERAL");
        }
    }

    private TaskPriority parsePriority(String rawPriority) {
        if (rawPriority == null) {
            return TaskPriority.MEDIUM;
        }
        try {
            return TaskPriority.valueOf(rawPriority);
        } catch (IllegalArgumentException ex) {
            throw new ValidationError("priority must be one of LOW, MEDIUM, HIGH, CRITICAL");
        }
    }
}
