package com.llburgers.messaging.consumer;

import com.llburgers.config.RabbitMQConfig;
import com.llburgers.domain.Notification;
import com.llburgers.domain.Order;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import com.llburgers.domain.enums.OrderStatus;
import com.llburgers.messaging.OrderCancelledMessage;
import com.llburgers.messaging.OrderPlacedMessage;
import com.llburgers.messaging.OrderStatusChangedMessage;
import com.llburgers.repository.NotificationRepository;
import com.llburgers.repository.OrderRepository;
import com.llburgers.service.IEmailService;
import com.llburgers.websocket.WebSocketPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * RabbitMQ consumer that processes order lifecycle messages.
 *
 * <p><b>Queues consumed:</b></p>
 * <ul>
 *     <li>{@code llburger.orders.placed}        → confirmation email (Mailjet) + notification</li>
 *     <li>{@code llburger.orders.status-changed} → status-update email (Mailjet) + notification</li>
 *     <li>{@code llburger.orders.cancelled}      → cancellation notification (in-app)</li>
 * </ul>
 *
 * <p>Each listener loads the full {@link Order} from the database so it always
 * works on the latest persisted state. Failed messages are automatically
 * routed to {@code llburger.dead-letter} by the container factory.</p>
 */
@Component
public class OrderMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderMessageConsumer.class);

    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final IEmailService emailService;
    private final WebSocketPushService wsPushService;

    public OrderMessageConsumer(OrderRepository orderRepository,
                                NotificationRepository notificationRepository,
                                IEmailService emailService,
                                WebSocketPushService wsPushService) {
        this.orderRepository = orderRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.wsPushService = wsPushService;
    }

    // ─── Order Placed ─────────────────────────────────────────────────────────

    /**
     * Sends order-confirmation email (Mailjet) and records a notification.
     * Triggered after a new order is persisted and stock is reduced.
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE)
    @Transactional
    public void handleOrderPlaced(OrderPlacedMessage message) {
        log.info("[MQ-CONSUME] order.placed → orderId={}", message.orderId());

        Order order = orderRepository.findById(message.orderId()).orElse(null);
        if (order == null) {
            log.warn("[MQ-CONSUME] Order not found in DB for id={}, skipping", message.orderId());
            return;
        }

        Notification notification = notificationRepository.save(Notification.builder()
                .order(order)
                .type(NotificationType.ORDER_PLACED)
                .status(NotificationStatus.PENDING)
                .build());

        try {
            emailService.sendOrderConfirmationEmail(order);
            notification.setStatus(NotificationStatus.SENT);
            log.info("[MQ-CONSUME] Order confirmation email SENT for orderId={}", order.getId());
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.error("[MQ-CONSUME] Order confirmation email FAILED for orderId={}: {}",
                    order.getId(), e.getMessage(), e);
            throw new RuntimeException("Email send failed for order " + order.getId(), e);
        } finally {
            notificationRepository.save(notification);
        }

        // Real-time WebSocket push — fires regardless of email outcome
        wsPushService.pushOrderEvent(notification, order);
    }

    // ─── Order Status Changed ─────────────────────────────────────────────────

    /**
     * Dispatches the right notification channel based on the new status:
     * <ul>
     *     <li>PROCESSING → in-app notification only (no email)</li>
     *     <li>ON_THE_WAY / DELIVERED → status-update email (Mailjet)</li>
     * </ul>
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_CHANGED_QUEUE)
    @Transactional
    public void handleOrderStatusChanged(OrderStatusChangedMessage message) {
        log.info("[MQ-CONSUME] order.status.changed → orderId={}, {} → {}",
                message.orderId(), message.previousStatus(), message.newStatus());

        Order order = orderRepository.findById(message.orderId()).orElse(null);
        if (order == null) {
            log.warn("[MQ-CONSUME] Order not found in DB for id={}, skipping", message.orderId());
            return;
        }

        OrderStatus newStatus = OrderStatus.valueOf(message.newStatus());

        Notification notification = notificationRepository.save(Notification.builder()
                .order(order)
                .type(NotificationType.ORDER_STATUS_CHANGED)
                .status(NotificationStatus.PENDING)
                .build());

        if (newStatus == OrderStatus.PROCESSING) {
            // In-app only — the frontend reads PENDING/SENT notifications from DB
            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);
            log.info("[MQ-CONSUME] In-app notification created for PROCESSING, orderId={}",
                    order.getId());
        } else {
            // ON_THE_WAY or DELIVERED → email via Mailjet
            try {
                emailService.sendOrderStatusUpdateEmail(order);
                notification.setStatus(NotificationStatus.SENT);
                log.info("[MQ-CONSUME] Status-update email SENT for orderId={}, status={}",
                        order.getId(), newStatus);
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);
                log.error("[MQ-CONSUME] Status-update email FAILED for orderId={}: {}",
                        order.getId(), e.getMessage(), e);
                throw new RuntimeException("Email send failed for order " + order.getId(), e);
            }
            notificationRepository.save(notification);
        }

        // Real-time WebSocket push for all status changes
        wsPushService.pushOrderEvent(notification, order);
    }

    // ─── Order Cancelled ──────────────────────────────────────────────────────

    /**
     * Creates an in-app cancellation notification.
     * (Stock is already restored by {@code OrderServiceImpl.cancel()}.)
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    @Transactional
    public void handleOrderCancelled(OrderCancelledMessage message) {
        log.info("[MQ-CONSUME] order.cancelled → orderId={}", message.orderId());

        // Order is deleted at this point; create a standalone notification log
        // via a CANCELLED type so the admin dashboard can see the event
        Notification notification = Notification.builder()
                .type(NotificationType.ORDER_CANCELLED)
                .status(NotificationStatus.SENT)
                .build();
        notificationRepository.save(notification);
        log.info("[MQ-CONSUME] Cancellation notification recorded for orderId={}",
                message.orderId());
    }
}
