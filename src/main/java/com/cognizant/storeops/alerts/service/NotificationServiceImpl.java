package com.cognizant.storeops.alerts.service;

import com.cognizant.storeops.alerts.model.AlertType;
import com.cognizant.storeops.alerts.model.Notification;
import com.cognizant.storeops.alerts.model.NotificationChannel;
import com.cognizant.storeops.alerts.model.NotificationStatus;
import com.cognizant.storeops.alerts.repository.NotificationRepository;
import com.cognizant.storeops.shared.error.ConflictError;
import com.cognizant.storeops.shared.error.NotFoundError;
import com.cognizant.storeops.shared.error.ValidationError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Notification raise(String recipientStaffId, AlertType alertType, NotificationChannel channel, String message) {
        if (recipientStaffId == null || recipientStaffId.isBlank()) {
            throw new ValidationError("recipientStaffId is required to raise an alert");
        }
        if (message == null || message.isBlank()) {
            throw new ValidationError("An alert must carry a non-blank message");
        }

        Notification notification = new Notification(recipientStaffId, alertType, channel, message);
        // IN_APP alerts are readable immediately; EMAIL stays PENDING until a
        // delivery adapter (out of scope for this scaffold) confirms send.
        if (channel == NotificationChannel.IN_APP) {
            notification.setStatus(NotificationStatus.SENT);
        }

        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> listForStaff(String recipientStaffId) {
        if (recipientStaffId == null || recipientStaffId.isBlank()) {
            throw new ValidationError("staffId query parameter is required");
        }
        return notificationRepository.findByRecipientStaffId(recipientStaffId);
    }

    @Override
    @Transactional
    public Notification markRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundError("Notification", notificationId));

        if (notification.getStatus() == NotificationStatus.READ) {
            throw new ConflictError("Notification " + notificationId + " has already been read");
        }

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(Instant.now());
        return notificationRepository.save(notification);
    }
}
