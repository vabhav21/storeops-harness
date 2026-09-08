package com.cognizant.storeops.staff.dto;

/** Request body for registering a member of store staff. */
public record RegisterStaffRequest(
        String email,
        String fullName,
        String role,
        String storeId,
        String regionId
) {
}
