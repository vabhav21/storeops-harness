package com.cognizant.storeops.alerts.routes;

import com.cognizant.storeops.alerts.dto.NotificationResponse;
import com.cognizant.storeops.alerts.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Routes layer for alerts. There is deliberately no POST /api/alerts
 * endpoint: alerts are raised from domain events inside this module, not
 * pushed in by callers, so exposing a create endpoint would let another
 * module bypass the event bus (architecture-principles Rule 2).
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final NotificationService notificationService;

    public AlertController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> listForStaff(@RequestParam String staffId) {
        List<NotificationResponse> alerts = notificationService.listForStaff(staffId).stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificationResponse.from(notificationService.markRead(id)));
    }
}
