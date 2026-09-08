package com.cognizant.storeops.programmes.routes;

import com.cognizant.storeops.programmes.dto.AddMemberRequest;
import com.cognizant.storeops.programmes.dto.ApplyTemplateRequest;
import com.cognizant.storeops.programmes.dto.CreateProjectRequest;
import com.cognizant.storeops.programmes.dto.ProjectResponse;
import com.cognizant.storeops.programmes.model.Project;
import com.cognizant.storeops.programmes.model.ProjectRole;
import com.cognizant.storeops.programmes.service.ProjectService;
import com.cognizant.storeops.shared.error.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Routes layer: HTTP binding, path/query validation, and delegation to
 * ProjectService. No business rules live here — see Section 3.5,
 * "Layer separation": routes must not contain business logic.
 */
@RestController
@RequestMapping("/api/programmes")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@RequestBody CreateProjectRequest request) {
        Project project = projectService.createProject(request.name(), request.storeId(), request.regionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectResponse.from(project));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ProjectResponse.from(projectService.getProject(id)));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String regionId) {

        List<Project> projects;
        if (storeId != null) {
            projects = projectService.listByStore(storeId);
        } else if (regionId != null) {
            projects = projectService.listByRegion(regionId);
        } else {
            throw new ValidationError("Provide either storeId or regionId as a query parameter");
        }
        return ResponseEntity.ok(projects.stream().map(ProjectResponse::from).toList());
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable UUID id,
            @RequestBody AddMemberRequest request) {

        ProjectRole role = parseRole(request.role());
        Project project = projectService.addMember(id, request.staffId(), role);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectResponse.from(project));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ProjectResponse> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ProjectResponse.from(projectService.closeProject(id)));
    }

    /**
     * Feature under harness governance for this capstone's demonstration
     * run — see PROMPT.md / .harness/output/spec.md.
     */
    @PostMapping("/{id}/templates")
    public ResponseEntity<ProjectResponse> applyTemplate(
            @PathVariable UUID id,
            @RequestBody ApplyTemplateRequest request) {

        Project project = projectService.applyTemplate(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ProjectResponse.from(project));
    }

    private ProjectRole parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new ValidationError("role is required");
        }
        try {
            return ProjectRole.valueOf(rawRole);
        } catch (IllegalArgumentException ex) {
            throw new ValidationError("role must be one of STORE_MANAGER, DEPARTMENT_LEAD, ASSOCIATE");
        }
    }
}
