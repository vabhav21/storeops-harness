package com.cognizant.storeops.programmes.service;

import com.cognizant.storeops.programmes.dto.ApplyTemplateRequest;
import com.cognizant.storeops.programmes.model.Project;
import com.cognizant.storeops.programmes.model.ProjectRole;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for the programmes module.
 *
 * This is the ONLY entry point other modules may call for programme
 * read-only lookups (e.g. reports aggregating across programmes). Modules
 * must not autowire ProjectRepository directly.
 */
public interface ProjectService {

    Project createProject(String name, String storeId, String regionId);

    Project getProject(UUID projectId);

    List<Project> listByStore(String storeId);

    List<Project> listByRegion(String regionId);

    Project addMember(UUID projectId, String staffId, ProjectRole role);

    Project closeProject(UUID projectId);

    /**
     * Clones a set of PLANOGRAM tasks into the given programme by emitting
     * PROGRAMME_TEMPLATE_APPLIED on the event bus. The activities module
     * owns actually creating the Task rows.
     */
    Project applyTemplate(UUID projectId, ApplyTemplateRequest request);
}
