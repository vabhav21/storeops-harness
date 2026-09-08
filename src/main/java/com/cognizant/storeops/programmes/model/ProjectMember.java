package com.cognizant.storeops.programmes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Membership of a staff member within a Project.
 *
 * staffId is intentionally a plain String reference, NOT a JPA foreign key
 * into the staff module's table. "users is read-only for other modules" —
 * the programmes module resolves staff details, when needed, through the
 * staff module's service layer (read-only lookup), never through a direct
 * repository join.
 */
@Entity
@Table(name = "project_members")
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "staff_id", nullable = false)
    private String staffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt = Instant.now();

    protected ProjectMember() {
        // JPA
    }

    public ProjectMember(String staffId, ProjectRole role) {
        this.staffId = staffId;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    void setProject(Project project) {
        this.project = project;
    }

    public String getStaffId() {
        return staffId;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
