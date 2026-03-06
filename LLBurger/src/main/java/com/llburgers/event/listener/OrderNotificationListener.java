package com.llburgers.event.listener;

import com.llburgers.domain.Notification;
import com.llburgers.domain.Order;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import com.llburgers.domain.enums.OrderStatus;
import com.llburgers.event.OrderPlacedEvent;
import com.llburgers.event.OrderStatusChangedEvent;
import com.llburgers.repository.NotificationRepository;
import com.llburgers.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens for order-lifecycle events and dispatches notifications.
 *
 * <p>Per the spec:</p>
 * <ul>
 *     <li><b>Order placed</b> → email confirmation (Mailjet)</li>
 *     <li><b>Status → PROCESSING</b> → in-app notification only</li>
 *     <li><b>Status → ON_THE_WAY / DELIVERED</b> → email notification (Mailjet)</li>
 * </ul>
 *
 * <p>All listeners fire <em>after</em> the originating transaction commits
 * ({@code TransactionPhase.AFTER_COMMIT}) to guarantee the order is persisted
 * before any side-effect runs. They execute asynchronously to avoid blocking
 * the caller.</p>
 */
@Component
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);

    private final NotificationRepository notificationRepository;
    private final IEmailService emailService;

    public OrderNotificationListener(NotificationRepository notificationRepository,
                                     IEmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    // ─── Order Placed → email ────────────────────────────────────────────────

    /**
     * Sends order-confirmation email (Mailjet) and records a notification.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderPlaced(OrderPlacedEvent event) {
        Order order = event.order();
        log.info("[EVENT] OrderPlacedEvent received – order={}", order.getId());

        Notification notification = notificationRepository.save(Notification.builder()
                .order(order)
                .type(NotificationType.ORDER_PLACED)
                .status(NotificationStatus.PENDING)
                .build());

        try {
            emailService.sendOrderConfirmationEmail(order);
            notification.setStatus(NotificationStatus.SENT);
            log.info("[EVENT] Order confirmation email SENT for order={}", order.getId());
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.error("[EVENT] Order confirmation email FAILED for order={}: {}",
                    order.getId(), e.getMessage());
        }
        notificationRepository.save(notification);
    }

    // ─── Order Status Changed ─────────────────────────────────────────────────

    /**
     * Dispatches the right notification channel based on the new status:
     * <ul>
     *     <li>PROCESSING → in-app notification only (no email)</li>
     *     <li>ON_THE_WAY / DELIVERED → status-update email (Mailjet)</li>
     * </ul>
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        Order order = event.order();
        OrderStatus newStatus = event.newStatus();

        log.info("[EVENT] OrderStatusChangedEvent – order={}, {} → {}",
                order.getId(), event.previousStatus(), newStatus);

        Notification notification = notificationRepository.save(Notification.builder()
                .order(order)
                .type(NotificationType.ORDER_STATUS_CHANGED)
                .status(NotificationStatus.PENDING)
                .build());

        if (newStatus == OrderStatus.PROCESSING) {
            // In-app only — mark as SENT immediately (the in-app channel
            // simply reads PENDING/SENT notifications from the DB)
            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);
            log.info("[EVENT] In-app notification created for PROCESSING status, order={}",
                    order.getId());
        } else {
            // ON_THE_WAY or DELIVERED → email via Mailjet
            try {
                emailService.sendOrderStatusUpdateEmail(order);
                notification.setStatus(NotificationStatus.SENT);
                log.info("[EVENT] Status-update email SENT for order={}, status={}",
                        order.getId(), newStatus);
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
                log.error("[EVENT] Status-update email FAILED for order={}: {}",
                        order.getId(), e.getMessage());
            }
            notificationRepository.save(notification);
        }
    }
}
