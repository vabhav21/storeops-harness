package com.cognizant.storeops.alerts.repository;

import com.cognizant.storeops.alerts.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Data-access layer for Notification. Only the alerts module may autowire this. */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientStaffId(String recipientStaffId);
}
