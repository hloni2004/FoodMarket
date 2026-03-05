package com.llburgers.repository;

import com.llburgers.domain.Notification;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // ─── By Order ─────────────────────────────────────────────────────────────

    List<Notification> findByOrderId(UUID orderId);

    List<Notification> findByOrderIdAndStatus(UUID orderId, NotificationStatus status);

    // ─── By Status ────────────────────────────────────────────────────────────

    List<Notification> findByStatus(NotificationStatus status);

    long countByStatus(NotificationStatus status);

    // ─── By Type ──────────────────────────────────────────────────────────────

    List<Notification> findByType(NotificationType type);

    List<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status);

    // ─── Business-level (no order) ────────────────────────────────────────────

    List<Notification> findByOrderIsNull();

    List<Notification> findByOrderIsNullAndType(NotificationType type);
}

