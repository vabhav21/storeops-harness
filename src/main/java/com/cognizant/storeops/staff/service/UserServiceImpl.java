package com.cognizant.storeops.staff.service;

import com.cognizant.storeops.shared.error.ConflictError;
import com.cognizant.storeops.shared.error.NotFoundError;
import com.cognizant.storeops.shared.error.ValidationError;
import com.cognizant.storeops.staff.dto.RegisterStaffRequest;
import com.cognizant.storeops.staff.model.StaffRole;
import com.cognizant.storeops.staff.model.User;
import com.cognizant.storeops.staff.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User registerStaff(RegisterStaffRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new ValidationError("email is required to register staff");
        }
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new ValidationError("fullName is required to register staff");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictError("Staff member with email " + request.email() + " is already registered");
        }

        User user = new User(request.email(), request.fullName(), parseRole(request.role()));
        user.setStoreId(request.storeId());
        user.setRegionId(request.regionId());

        return userRepository.save(user);
    }

    @Override
    public User getStaff(UUID staffId) {
        return userRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundError("User", staffId));
    }

    @Override
    public List<User> listByStore(String storeId) {
        return userRepository.findByStoreId(storeId);
    }

    private StaffRole parseRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            throw new ValidationError("role is required");
        }
        try {
            return StaffRole.valueOf(rawRole);
        } catch (IllegalArgumentException ex) {
            throw new ValidationError(
                    "role must be one of REGIONAL_MANAGER, STORE_MANAGER, DEPARTMENT_LEAD, ASSOCIATE");
        }
    }
}
