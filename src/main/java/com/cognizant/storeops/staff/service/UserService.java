package com.cognizant.storeops.staff.service;

import com.cognizant.storeops.staff.dto.RegisterStaffRequest;
import com.cognizant.storeops.staff.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for the staff module.
 *
 * getStaff and listByStore are the read-only lookups other modules are
 * permitted to call. Authentication (AuthToken issuance) is intentionally
 * out of scope for this reference scaffold — it would be added here rather
 * than in any consuming module.
 */
public interface UserService {

    User registerStaff(RegisterStaffRequest request);

    User getStaff(UUID staffId);

    List<User> listByStore(String storeId);
}
