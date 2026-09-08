package com.cognizant.storeops.programmes.repository;

import com.cognizant.storeops.programmes.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Data-access layer for Project. This interface (and its Spring Data
 * proxy) is the ONLY class permitted to issue queries against the
 * projects/project_members tables. Other modules must not autowire this
 * repository directly — they call ProjectService instead (module boundary
 * rule, Section 3.5 of the capstone spec).
 */
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByStoreId(String storeId);

    List<Project> findByRegionId(String regionId);
}
