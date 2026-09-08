package com.cognizant.storeops.reports.model;

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
 * A generated performance summary.
 *
 * The reports table is the ONLY table the reports module writes to. Rule 5
 * (read-only reports): reports aggregates across activities, programmes,
 * and staff via their service layers but never persists anything back into
 * them.
 */
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    /** Store id for STORE_SUMMARY, region id for REGIONAL_ROLLUP. */
    @Column(name = "scope_id", nullable = false)
    private String scopeId;

    @Column(name = "task_count", nullable = false)
    private int taskCount;

    @Column(name = "completed_count", nullable = false)
    private int completedCount;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt = Instant.now();

    protected Report() {
        // JPA
    }

    public Report(ReportType type, String scopeId) {
        this.type = type;
        this.scopeId = scopeId;
    }

    public UUID getId() {
        return id;
    }

    public ReportType getType() {
        return type;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getScopeId() {
        return scopeId;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
