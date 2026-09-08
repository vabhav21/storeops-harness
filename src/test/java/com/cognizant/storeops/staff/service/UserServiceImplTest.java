package com.cognizant.storeops.staff.service;

import com.cognizant.storeops.shared.error.ConflictError;
import com.cognizant.storeops.shared.error.NotFoundError;
import com.cognizant.storeops.shared.error.ValidationError;
import com.cognizant.storeops.staff.dto.RegisterStaffRequest;
import com.cognizant.storeops.staff.model.StaffRole;
import com.cognizant.storeops.staff.model.User;
import com.cognizant.storeops.staff.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Business-rule tests for the staff UserService. */
class UserServiceImplTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void registerStaff_givenBlankEmail_thenThrowsValidationErrorAndPersistsNothing() {
        RegisterStaffRequest request = new RegisterStaffRequest(
                " ", "Asha Patel", "ASSOCIATE", "store-1", "region-1");

        assertThatThrownBy(() -> userService.registerStaff(request))
                .isInstanceOf(ValidationError.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerStaff_givenUnknownRole_thenThrowsValidationError() {
        RegisterStaffRequest request = new RegisterStaffRequest(
                "asha@example.com", "Asha Patel", "SHIFT_WIZARD", "store-1", "region-1");

        assertThatThrownBy(() -> userService.registerStaff(request))
                .isInstanceOf(ValidationError.class);
    }

    @Test
    void registerStaff_givenAlreadyRegisteredEmail_thenThrowsConflictError() {
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(true);
        RegisterStaffRequest request = new RegisterStaffRequest(
                "asha@example.com", "Asha Patel", "ASSOCIATE", "store-1", "region-1");

        assertThatThrownBy(() -> userService.registerStaff(request))
                .isInstanceOf(ConflictError.class);
    }

    @Test
    void registerStaff_givenValidRequest_thenStoresRoleAndStoreAssignment() {
        RegisterStaffRequest request = new RegisterStaffRequest(
                "lead@example.com", "Dev Kumar", "DEPARTMENT_LEAD", "store-4", "region-2");

        User user = userService.registerStaff(request);

        assertThat(user.getRole()).isEqualTo(StaffRole.DEPARTMENT_LEAD);
        assertThat(user.getStoreId()).isEqualTo("store-4");
        assertThat(user.getRegionId()).isEqualTo("region-2");
    }

    @Test
    void getStaff_givenUnknownId_thenThrowsNotFoundError() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getStaff(missingId))
                .isInstanceOf(NotFoundError.class);
    }
}
