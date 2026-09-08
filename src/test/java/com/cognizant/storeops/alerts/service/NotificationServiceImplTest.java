package com.cognizant.storeops.alerts.service;

import com.cognizant.storeops.alerts.model.AlertType;
import com.cognizant.storeops.alerts.model.Notification;
import com.cognizant.storeops.alerts.model.NotificationChannel;
import com.cognizant.storeops.alerts.model.NotificationStatus;
import com.cognizant.storeops.alerts.repository.NotificationRepository;
import com.cognizant.storeops.shared.error.ConflictError;
import com.cognizant.storeops.shared.error.NotFoundError;
import com.cognizant.storeops.shared.error.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Business-rule tests for the alerts NotificationService. */
class NotificationServiceImplTest {

    private NotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        notificationService = new NotificationServiceImpl(notificationRepository);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void raise_givenBlankRecipient_thenThrowsValidationError() {
        assertThatThrownBy(() -> notificationService.raise(
                " ", AlertType.SLA_BREACH, NotificationChannel.IN_APP, "Task overdue"))
                .isInstanceOf(ValidationError.class);
    }

    @Test
    void raise_givenInAppChannel_thenStatusIsSentImmediately() {
        Notification raised = notificationService.raise(
                "staff-1", AlertType.SHIFT_HANDOVER, NotificationChannel.IN_APP, "Handover pending");

        assertThat(raised.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(raised.getAlertType()).isEqualTo(AlertType.SHIFT_HANDOVER);
    }

    @Test
    void raise_givenEmailChannel_thenStatusStaysPendingUntilDeliveryConfirmed() {
        Notification raised = notificationService.raise(
                "staff-1", AlertType.ESCALATION, NotificationChannel.EMAIL, "Escalated to store manager");

        assertThat(raised.getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void markRead_givenUnknownId_thenThrowsNotFoundError() {
        UUID missingId = UUID.randomUUID();
        when(notificationRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markRead(missingId))
                .isInstanceOf(NotFoundError.class);
    }

    @Test
    void markRead_givenAlreadyReadNotification_thenThrowsConflictError() {
        Notification read = new Notification(
                "staff-1", AlertType.INVENTORY, NotificationChannel.IN_APP, "Low stock");
        read.setStatus(NotificationStatus.READ);
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.of(read));

        assertThatThrownBy(() -> notificationService.markRead(id))
                .isInstanceOf(ConflictError.class);
    }

    @Test
    void markRead_givenSentNotification_thenStampsReadAt() {
        Notification sent = new Notification(
                "staff-1", AlertType.INVENTORY, NotificationChannel.IN_APP, "Low stock");
        sent.setStatus(NotificationStatus.SENT);
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.of(sent));

        Notification updated = notificationService.markRead(id);

        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(updated.getReadAt()).isNotNull();
    }
}
