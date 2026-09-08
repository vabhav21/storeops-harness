package com.cognizant.storeops.activities.routes;

import com.cognizant.storeops.activities.dto.CreateTaskRequest;
import com.cognizant.storeops.activities.dto.TaskResponse;
import com.cognizant.storeops.activities.dto.UpdateTaskStatusRequest;
import com.cognizant.storeops.activities.model.Task;
import com.cognizant.storeops.activities.model.TaskStatus;
import com.cognizant.storeops.activities.service.TaskService;
import com.cognizant.storeops.shared.error.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Routes layer for operational activities: HTTP binding, enum parsing, and
 * delegation to TaskService. Business rules (which status transitions are
 * legal, when TASK_COMPLETED is emitted) live in the service — Section 3.5,
 * "Layer separation".
 */
@RestController
@RequestMapping("/api/activities")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(TaskResponse.from(taskService.getTask(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody UpdateTaskStatusRequest request) {

        Task task = taskService.updateStatus(id, parseStatus(request.status()));
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    private TaskStatus parseStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new ValidationError("status is required");
        }
        try {
            return TaskStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException ex) {
            throw new ValidationError("status must be one of TODO, IN_PROGRESS, DONE, BLOCKED");
        }
    }
}
