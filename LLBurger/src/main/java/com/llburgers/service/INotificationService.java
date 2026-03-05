package com.llburgers.service;

import com.llburgers.domain.Notification;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface INotificationService extends IService<Notification, UUID> {

    // ─── By Order ─────────────────────────────────────────────────────────────

    List<Notification> findByOrderId(UUID orderId);

    List<Notification> findByOrderIdAndStatus(UUID orderId, NotificationStatus status);

    // ─── By Status ────────────────────────────────────────────────────────────

    List<Notification> findByStatus(NotificationStatus status);

    long countByStatus(NotificationStatus status);

    // ─── By Type ──────────────────────────────────────────────────────────────

    List<Notification> findByType(NotificationType type);

    List<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status);

    // ─── Business-level ───────────────────────────────────────────────────────

    List<Notification> findBusinessNotifications();

    List<Notification> findBusinessNotificationsByType(NotificationType type);

    // ─── Business Logic ───────────────────────────────────────────────────────

    Notification markAsSent(UUID id);

    Notification markAsFailed(UUID id);
}

