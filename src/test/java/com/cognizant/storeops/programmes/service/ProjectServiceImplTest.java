package com.cognizant.storeops.programmes.service;

import com.cognizant.storeops.programmes.dto.ApplyTemplateRequest;
import com.cognizant.storeops.programmes.dto.TemplateTaskSpec;
import com.cognizant.storeops.programmes.model.Project;
import com.cognizant.storeops.programmes.model.ProjectRole;
import com.cognizant.storeops.programmes.model.ProjectStatus;
import com.cognizant.storeops.programmes.repository.ProjectRepository;
import com.cognizant.storeops.shared.error.ConflictError;
import com.cognizant.storeops.shared.error.NotFoundError;
import com.cognizant.storeops.shared.error.ValidationError;
import com.cognizant.storeops.shared.events.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GIVEN/WHEN/THEN-style unit tests for the programmes ProjectService.
 * Verifies business-rule compliance (per Section 2 failure mode #3: tests
 * must check business rules, not just status codes) rather than HTTP
 * concerns, which belong to a separate MockMvc test on ProjectController.
 */
class ProjectServiceImplTest {

    private ProjectRepository projectRepository;
    private EventBus eventBus;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        eventBus = mock(EventBus.class);
        projectService = new ProjectServiceImpl(projectRepository, eventBus);
        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createProject_givenBlankName_thenThrowsValidationError() {
        assertThatThrownBy(() -> projectService.createProject("  ", "store-1", "region-1"))
                .isInstanceOf(ValidationError.class);
    }

    @Test
    void createProject_givenValidInput_thenStatusIsActive() {
        Project project = projectService.createProject("Winter Refit", "store-1", "region-1");

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(project.getStoreId()).isEqualTo("store-1");
    }

    @Test
    void getProject_givenUnknownId_thenThrowsNotFoundError() {
        UUID missingId = UUID.randomUUID();
        when(projectRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(missingId))
                .isInstanceOf(NotFoundError.class);
    }

    @Test
    void closeProject_givenAlreadyClosedProgramme_thenThrowsConflictError() {
        Project closed = new Project("Compliance Drive", "store-2", "region-1");
        closed.setStatus(ProjectStatus.CLOSED);
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> projectService.closeProject(id))
                .isInstanceOf(ConflictError.class);
    }

    @Test
    void closeProject_givenActiveProgramme_thenEmitsProgrammeClosedEvent() {
        Project active = new Project("Seasonal Rollout", "store-3", "region-2");
        active.setStatus(ProjectStatus.ACTIVE);
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.of(active));

        projectService.closeProject(id);

        verify(eventBus).emit("PROGRAMME_CLOSED", active.getId());
    }

    @Test
    void applyTemplate_givenEmptyTaskList_thenThrowsValidationError() {
        Project active = new Project("Planogram Reset", "store-4", "region-2");
        active.setStatus(ProjectStatus.ACTIVE);
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.of(active));

        ApplyTemplateRequest request = new ApplyTemplateRequest("Standard Planogram", List.of());

        assertThatThrownBy(() -> projectService.applyTemplate(id, request))
                .isInstanceOf(ValidationError.class);
    }

    @Test
    void applyTemplate_givenClosedProgramme_thenThrowsConflictError() {
        Project closed = new Project("Old Refit", "store-5", "region-2");
        closed.setStatus(ProjectStatus.CLOSED);
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.of(closed));

        ApplyTemplateRequest request = new ApplyTemplateRequest(
                "Standard Planogram",
                List.of(new TemplateTaskSpec("Reset endcap 3", "Grocery", "HIGH", "PLANOGRAM")));

        assertThatThrownBy(() -> projectService.applyTemplate(id, request))
                .isInstanceOf(ConflictError.class);
    }

    @Test
    void applyTemplate_givenActiveProgrammeAndTasks_thenEmitsEventNotDirectRepositoryWrite() {
        Project active = new Project("New Store Setup", "store-6", "region-3");
        active.setStatus(ProjectStatus.ACTIVE);
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.of(active));

        ApplyTemplateRequest request = new ApplyTemplateRequest(
                "Standard Planogram",
                List.of(
                        new TemplateTaskSpec("Reset endcap 3", "Grocery", "HIGH", "PLANOGRAM"),
                        new TemplateTaskSpec("Reset aisle 7", "Dairy", "MEDIUM", "PLANOGRAM")));

        projectService.applyTemplate(id, request);

        ArgumentCaptor<ProgrammeTemplateAppliedEvent> captor =
                ArgumentCaptor.forClass(ProgrammeTemplateAppliedEvent.class);
        verify(eventBus).emit(eq(ProgrammeTemplateAppliedEvent.EVENT_TYPE), captor.capture());

        ProgrammeTemplateAppliedEvent payload = captor.getValue();
        assertThat(payload.tasks()).hasSize(2);
        assertThat(payload.projectId()).isEqualTo(active.getId());
    }
}
