package com.cognizant.storeops.programmes.service;

import com.cognizant.storeops.programmes.dto.ApplyTemplateRequest;
import com.cognizant.storeops.programmes.model.Project;
import com.cognizant.storeops.programmes.model.ProjectMember;
import com.cognizant.storeops.programmes.model.ProjectRole;
import com.cognizant.storeops.programmes.model.ProjectStatus;
import com.cognizant.storeops.programmes.repository.ProjectRepository;
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
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EventBus eventBus;

    public ProjectServiceImpl(ProjectRepository projectRepository, EventBus eventBus) {
        this.projectRepository = projectRepository;
        this.eventBus = eventBus;
    }

    @Override
    @Transactional
    public Project createProject(String name, String storeId, String regionId) {
        if (name == null || name.isBlank()) {
            throw new ValidationError("Programme name must not be blank");
        }
        if (storeId == null || storeId.isBlank()) {
            throw new ValidationError("storeId is required to create a programme");
        }
        Project project = new Project(name, storeId, regionId);
        project.setStatus(ProjectStatus.ACTIVE);
        return projectRepository.save(project);
    }

    @Override
    public Project getProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundError("Project", projectId));
    }

    @Override
    public List<Project> listByStore(String storeId) {
        return projectRepository.findByStoreId(storeId);
    }

    @Override
    public List<Project> listByRegion(String regionId) {
        return projectRepository.findByRegionId(regionId);
    }

    @Override
    @Transactional
    public Project addMember(UUID projectId, String staffId, ProjectRole role) {
        Project project = getProject(projectId);

        if (project.getStatus() == ProjectStatus.CLOSED) {
            throw new ConflictError("Cannot add a member to a CLOSED programme");
        }
        boolean alreadyMember = project.getMembers().stream()
                .anyMatch(m -> m.getStaffId().equals(staffId));
        if (alreadyMember) {
            throw new ConflictError("Staff member " + staffId + " is already on this programme");
        }

        project.addMember(new ProjectMember(staffId, role));
        return projectRepository.save(project);
    }

    @Override
    @Transactional
    public Project closeProject(UUID projectId) {
        Project project = getProject(projectId);

        if (project.getStatus() == ProjectStatus.CLOSED) {
            throw new ConflictError("Programme " + projectId + " is already closed");
        }

        project.setStatus(ProjectStatus.CLOSED);
        project.setClosedAt(Instant.now());
        Project saved = projectRepository.save(project);

        // Cross-module side effect: reports module listens for this event
        // and generates a STORE_SUMMARY report. No direct import of the
        // reports module's ReportService is permitted here.
        eventBus.emit("PROGRAMME_CLOSED", saved.getId());

        return saved;
    }

    @Override
    @Transactional
    public Project applyTemplate(UUID projectId, ApplyTemplateRequest request) {
        Project project = getProject(projectId);

        if (project.getStatus() == ProjectStatus.CLOSED) {
            throw new ConflictError("Cannot apply a task template to a CLOSED programme");
        }
        if (request.tasks() == null || request.tasks().isEmpty()) {
            throw new ValidationError("Template must contain at least one task");
        }

        // Cross-module side effect: the activities module owns Task
        // creation. programmes never writes to the activities repository —
        // it only emits the intent on the event bus (module boundary +
        // event-bus-only rules, Section 3.5).
        eventBus.emit(
                ProgrammeTemplateAppliedEvent.EVENT_TYPE,
                new ProgrammeTemplateAppliedEvent(
                        project.getId(),
                        project.getStoreId(),
                        request.templateName(),
                        request.tasks()
                )
        );

        return project;
    }
}
