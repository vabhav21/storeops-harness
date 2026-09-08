package com.cognizant.storeops.programmes.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A store programme — e.g. a seasonal rollout, a compliance drive, or a
 * store refit. Owns its membership (ProjectMember) but never writes to the
 * activities, staff, alerts, or reports modules directly. Cross-module
 * effects (e.g. cloning PLANOGRAM tasks into activities on template
 * application) are raised via the EventBus per the module boundary rule.
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "region_id", nullable = false)
    private String regionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMember> members = new ArrayList<>();

    protected Project() {
        // JPA
    }

    public Project(String name, String storeId, String regionId) {
        this.name = name;
        this.storeId = storeId;
        this.regionId = regionId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getRegionId() {
        return regionId;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public List<ProjectMember> getMembers() {
        return members;
    }

    public void addMember(ProjectMember member) {
        member.setProject(this);
        this.members.add(member);
    }
}
