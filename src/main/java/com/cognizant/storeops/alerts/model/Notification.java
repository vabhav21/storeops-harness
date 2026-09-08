package com.cognizant.storeops.alerts.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * An in-app or email alert raised for a member of store staff.
 *
 * Notifications are only ever created by the alerts module itself, in
 * response to an event received on the EventBus. No other module writes
 * to this table, and the alerts module does not import any other
 * module's service to decide whether to raise one (Rule 2).
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_staff_id", nullable = false)
    private String recipientStaffId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel = NotificationChannel.IN_APP;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() {
        // JPA
    }

    public Notification(String recipientStaffId, AlertType alertType, NotificationChannel channel, String message) {
        this.recipientStaffId = recipientStaffId;
        this.alertType = alertType;
        this.channel = channel;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public String getRecipientStaffId() {
        return recipientStaffId;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }
}
