package com.cognizant.storeops.activities.repository;

import com.cognizant.storeops.activities.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Data-access layer for Task. This interface is the ONLY class permitted
 * to query the tasks table. The reports module aggregates activity data
 * through TaskService, never by autowiring this repository (module
 * boundary rule, Section 3.5).
 */
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByStoreId(String storeId);

    List<Task> findByProjectId(UUID projectId);
}
