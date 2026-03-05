package com.llburgers.service.impl;

import com.llburgers.domain.Notification;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import com.llburgers.repository.NotificationRepository;
import com.llburgers.service.INotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository repository;

    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    // ─── CRUD (from IService) ─────────────────────────────────────────────────

    @Override
    public Notification create(Notification notification) {
        return repository.save(notification);
    }

    @Override
    public Notification read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
    }

    @Override
    public Notification update(Notification notification) {
        if (notification.getId() == null || !repository.existsById(notification.getId())) {
            throw new IllegalArgumentException("Notification not found with id: " + notification.getId());
        }
        return repository.save(notification);
    }

    @Override
    public List<Notification> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Notification not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ─── By Order ─────────────────────────────────────────────────────────────

    @Override
    public List<Notification> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId);
    }

    @Override
    public List<Notification> findByOrderIdAndStatus(UUID orderId, NotificationStatus status) {
        return repository.findByOrderIdAndStatus(orderId, status);
    }

    // ─── By Status ────────────────────────────────────────────────────────────

    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        return repository.findByStatus(status);
    }

    @Override
    public long countByStatus(NotificationStatus status) {
        return repository.countByStatus(status);
    }

    // ─── By Type ──────────────────────────────────────────────────────────────

    @Override
    public List<Notification> findByType(NotificationType type) {
        return repository.findByType(type);
    }

    @Override
    public List<Notification> findByTypeAndStatus(NotificationType type, NotificationStatus status) {
        return repository.findByTypeAndStatus(type, status);
    }

    // ─── Business-level ───────────────────────────────────────────────────────

    @Override
    public List<Notification> findBusinessNotifications() {
        return repository.findByOrderIsNull();
    }

    @Override
    public List<Notification> findBusinessNotificationsByType(NotificationType type) {
        return repository.findByOrderIsNullAndType(type);
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @Override
    public Notification markAsSent(UUID id) {
        Notification notification = read(id);
        notification.setStatus(NotificationStatus.SENT);
        return repository.save(notification);
    }

    @Override
    public Notification markAsFailed(UUID id) {
        Notification notification = read(id);
        notification.setStatus(NotificationStatus.FAILED);
        return repository.save(notification);
    }
}

