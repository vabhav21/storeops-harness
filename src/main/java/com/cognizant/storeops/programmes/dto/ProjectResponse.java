package com.cognizant.storeops.programmes.dto;

import com.cognizant.storeops.programmes.model.Project;
import com.cognizant.storeops.programmes.model.ProjectMember;
import com.cognizant.storeops.programmes.model.ProjectStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Read-model returned by programme routes. Keeps the entity out of the HTTP layer. */
public record ProjectResponse(
        UUID id,
        String name,
        String storeId,
        String regionId,
        ProjectStatus status,
        Instant createdAt,
        Instant closedAt,
        List<MemberView> members
) {

    public record MemberView(UUID id, String staffId, String role, Instant addedAt) {
    }

    public static ProjectResponse from(Project project) {
        List<MemberView> members = project.getMembers().stream()
                .map(ProjectResponse::toMemberView)
                .collect(Collectors.toList());

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getStoreId(),
                project.getRegionId(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getClosedAt(),
                members
        );
    }

    private static MemberView toMemberView(ProjectMember member) {
        return new MemberView(
                member.getId(),
                member.getStaffId(),
                member.getRole().name(),
                member.getAddedAt()
        );
    }
}
