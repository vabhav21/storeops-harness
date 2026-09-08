package com.cognizant.storeops.staff.dto;

import com.cognizant.storeops.staff.model.StaffRole;
import com.cognizant.storeops.staff.model.User;

import java.time.Instant;
import java.util.UUID;

/** Read-model returned by staff routes. Never exposes the User entity directly. */
public record StaffResponse(
        UUID id,
        String email,
        String fullName,
        StaffRole role,
        String storeId,
        String regionId,
        Instant createdAt
) {

    public static StaffResponse from(User user) {
        return new StaffResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStoreId(),
                user.getRegionId(),
                user.getCreatedAt()
        );
    }
}
