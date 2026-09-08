package com.cognizant.storeops.staff.routes;

import com.cognizant.storeops.staff.dto.RegisterStaffRequest;
import com.cognizant.storeops.staff.dto.StaffResponse;
import com.cognizant.storeops.staff.model.User;
import com.cognizant.storeops.staff.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Routes layer for store staff: HTTP binding and delegation to UserService only. */
@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final UserService userService;

    public StaffController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<StaffResponse> register(@RequestBody RegisterStaffRequest request) {
        User user = userService.registerStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(StaffResponse.from(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(StaffResponse.from(userService.getStaff(id)));
    }
}
