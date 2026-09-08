package com.cognizant.storeops.staff.model;

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
 * A member of store staff.
 *
 * The staff module is read-only for every other module: programmes,
 * activities, alerts, and reports may look a User up through
 * UserService but must never write to it, and must never join to this
 * table from their own repositories (module boundary rule, Section 3.5).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffRole role;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "region_id")
    private String regionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected User() {
        // JPA
    }

    public User(String email, String fullName, StaffRole role) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public StaffRole getRole() {
        return role;
    }

    public void setRole(StaffRole role) {
        this.role = role;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
