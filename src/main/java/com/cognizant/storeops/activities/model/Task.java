package com.cognizant.storeops.activities.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * An operational activity — a restocking run, planogram reset, compliance
 * check, or general store task.
 *
 * projectId and assigneeStaffId are plain reference columns, NOT JPA
 * foreign keys into the programmes or staff tables. The activities module
 * resolves those details, when it needs them, through the owning module's
 * service layer (read-only lookup) — never through a cross-module
 * repository join (module boundary rule, Section 3.5).
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column
    private String department;

    @Column(name = "assignee_staff_id")
    private String assigneeStaffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskCategory category = TaskCategory.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    protected Task() {
        // JPA
    }

    public Task(String title, String storeId, TaskCategory category, TaskPriority priority) {
        this.title = title;
        this.storeId = storeId;
        this.category = category;
        this.priority = priority;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStoreId() {
        return storeId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAssigneeStaffId() {
        return assigneeStaffId;
    }

    public void setAssigneeStaffId(String assigneeStaffId) {
        this.assigneeStaffId = assigneeStaffId;
    }

    public TaskCategory getCategory() {
        return category;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
