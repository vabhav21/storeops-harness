package com.cognizant.storeops.alerts.dto;

import com.cognizant.storeops.alerts.model.AlertType;
import com.cognizant.storeops.alerts.model.Notification;
import com.cognizant.storeops.alerts.model.NotificationChannel;
import com.cognizant.storeops.alerts.model.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

/** Read-model returned by alert routes. */
public record NotificationResponse(
        UUID id,
        String recipientStaffId,
        AlertType alertType,
        NotificationChannel channel,
        NotificationStatus status,
        String message,
        Instant createdAt,
        Instant readAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientStaffId(),
                notification.getAlertType(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
