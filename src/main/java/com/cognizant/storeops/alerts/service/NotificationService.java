package com.cognizant.storeops.alerts.service;

import com.cognizant.storeops.alerts.model.AlertType;
import com.cognizant.storeops.alerts.model.Notification;
import com.cognizant.storeops.alerts.model.NotificationChannel;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for the alerts module.
 *
 * raise(...) is called by event listeners inside this module only. Other
 * modules must NOT call it to push an alert — they emit a domain event and
 * let alerts decide whether an alert is warranted (architecture-principles
 * Rule 2). That inversion is what keeps alerting policy in one module
 * instead of scattered across every caller.
 */
public interface NotificationService {

    Notification raise(String recipientStaffId, AlertType alertType, NotificationChannel channel, String message);

    List<Notification> listForStaff(String recipientStaffId);

    Notification markRead(UUID notificationId);
}
