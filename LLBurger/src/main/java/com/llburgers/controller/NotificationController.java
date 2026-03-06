package com.llburgers.controller;

import com.llburgers.domain.Notification;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import com.llburgers.service.INotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    private final INotificationService notificationService;

    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(notification));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> read(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.read(id));
    }

    @PutMapping
    public ResponseEntity<Notification> update(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.update(notification));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── By Order ─────────────────────────────────────────────────────────────

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Notification>> findByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(notificationService.findByOrderId(orderId));
    }

    @GetMapping("/order/{orderId}/status/{status}")
    public ResponseEntity<List<Notification>> findByOrderIdAndStatus(
            @PathVariable UUID orderId, @PathVariable NotificationStatus status) {
        return ResponseEntity.ok(notificationService.findByOrderIdAndStatus(orderId, status));
    }

    // ─── By Status ────────────────────────────────────────────────────────────

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> findByStatus(@PathVariable NotificationStatus status) {
        return ResponseEntity.ok(notificationService.findByStatus(status));
    }

    @GetMapping("/status/{status}/count")
    public ResponseEntity<Long> countByStatus(@PathVariable NotificationStatus status) {
        return ResponseEntity.ok(notificationService.countByStatus(status));
    }

    // ─── By Type ──────────────────────────────────────────────────────────────

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> findByType(@PathVariable NotificationType type) {
        return ResponseEntity.ok(notificationService.findByType(type));
    }

    @GetMapping("/type/{type}/status/{status}")
    public ResponseEntity<List<Notification>> findByTypeAndStatus(
            @PathVariable NotificationType type, @PathVariable NotificationStatus status) {
        return ResponseEntity.ok(notificationService.findByTypeAndStatus(type, status));
    }

    // ─── Business-level ───────────────────────────────────────────────────────

    @GetMapping("/business")
    public ResponseEntity<List<Notification>> findBusinessNotifications() {
        return ResponseEntity.ok(notificationService.findBusinessNotifications());
    }

    @GetMapping("/business/type/{type}")
    public ResponseEntity<List<Notification>> findBusinessNotificationsByType(@PathVariable NotificationType type) {
        return ResponseEntity.ok(notificationService.findBusinessNotificationsByType(type));
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/sent")
    public ResponseEntity<Notification> markAsSent(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsSent(id));
    }

    @PatchMapping("/{id}/failed")
    public ResponseEntity<Notification> markAsFailed(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsFailed(id));
    }
}
