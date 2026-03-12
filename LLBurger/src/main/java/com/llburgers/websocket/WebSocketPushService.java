package com.llburgers.websocket;

import com.llburgers.domain.Notification;
import com.llburgers.domain.Order;
import com.llburgers.domain.enums.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Sends real-time WebSocket pushes via STOMP using Spring's
 * {@link SimpMessagingTemplate}.
 *
 * <h3>Topics</h3>
 * <pre>
 *   /topic/orders             – admin dashboard subscribes here
 *   /topic/order/{orderId}    – customer subscribes when tracking an order
 *   /topic/business           – all clients subscribe for open/close events
 * </pre>
 *
 * <p>This service is called by the RabbitMQ consumers
 * ({@link com.llburgers.messaging.consumer.OrderMessageConsumer} and
 * {@link com.llburgers.messaging.consumer.BusinessStatusMessageConsumer})
 * after they have persisted the {@link Notification} to the database,
 * so the push always reflects a committed state.</p>
 */
@Service
public class WebSocketPushService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketPushService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ─── Order Events ─────────────────────────────────────────────────────────

    /**
     * Pushes an order-lifecycle event to:
     * <ol>
     *     <li>{@code /topic/orders}          – admin-level feed</li>
     *     <li>{@code /topic/order/{orderId}} – customer order-tracker</li>
     * </ol>
     *
     * @param notification the persisted notification (provides IDs + type)
     * @param order        the full order object (provides status + customer ID)
     */
    public void pushOrderEvent(Notification notification, Order order) {
        String humanMessage = buildOrderMessage(notification.getType(), order);

        OrderNotificationPayload payload = new OrderNotificationPayload(
                notification.getId(),
                order.getId(),
                order.getCustomer().getId(),
                notification.getType(),
                order.getStatus(),
                humanMessage,
                LocalDateTime.now());

        // 1. Admin sees all order events
        messagingTemplate.convertAndSend("/topic/orders", payload);

        // 2. Customer tracking their specific order
        String orderTopic = "/topic/order/" + order.getId();
        messagingTemplate.convertAndSend(orderTopic, payload);

        log.info("[WS-PUSH] Order event pushed – type={}, orderId={}, topics=[/topic/orders, {}]",
                notification.getType(), order.getId(), orderTopic);
    }

    // ─── Business Status Events ───────────────────────────────────────────────

    /**
     * Pushes a business open/close event to {@code /topic/business}.
     * All connected clients (customers + admins) receive this.
     *
     * @param notification  the persisted notification
     * @param closedMessage optional admin message (null for "opened" events)
     */
    public void pushBusinessEvent(Notification notification, String closedMessage) {
        String humanMessage = notification.getType() == NotificationType.BUSINESS_OPENED
                ? "We are now OPEN — place your order!"
                : (closedMessage != null && !closedMessage.isBlank())
                        ? "We are now CLOSED. " + closedMessage
                        : "We are now CLOSED.";

        BusinessStatusPayload payload = new BusinessStatusPayload(
                notification.getId(),
                notification.getType(),
                humanMessage,
                LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/business", payload);

        log.info("[WS-PUSH] Business event pushed – type={}", notification.getType());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String buildOrderMessage(NotificationType type, Order order) {
        return switch (type) {
            case ORDER_PLACED         -> "Your order has been received and is being processed.";
            case ORDER_STATUS_CHANGED -> switch (order.getStatus()) {
                case PROCESSING  -> "Your order is being prepared.";
                case ON_THE_WAY  -> "Your order is on the way! Block "
                        + order.getDeliveryBlock() + ", Room " + order.getDeliveryRoomNumber() + ".";
                case DELIVERED   -> "Your order has been delivered. Enjoy!";
                default          -> "Your order status has been updated to: " + order.getStatus();
            };
            case ORDER_CANCELLED      -> "Your order has been cancelled.";
            default                   -> "Order update received.";
        };
    }
}
