package com.cognizant.storeops.staff.repository;

import com.cognizant.storeops.staff.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Data-access layer for User. Only the staff module may autowire this —
 * other modules resolve staff through UserService (read-only lookup).
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    List<User> findByStoreId(String storeId);
}
