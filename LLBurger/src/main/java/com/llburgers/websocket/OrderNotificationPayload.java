package com.llburgers.websocket;

import com.llburgers.domain.enums.NotificationType;
import com.llburgers.domain.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload pushed over WebSocket for every order-lifecycle event.
 *
 * <p>Sent on:</p>
 * <ul>
 *     <li>{@code /topic/orders}           – admin dashboard (all orders)</li>
 *     <li>{@code /topic/order/{orderId}}  – customer tracking their specific order</li>
 * </ul>
 *
 * @param notificationId  the persisted {@link com.llburgers.domain.Notification} ID
 * @param orderId         the affected order
 * @param customerId      customer who owns the order
 * @param type            notification type (ORDER_PLACED, ORDER_STATUS_CHANGED, ORDER_CANCELLED)
 * @param orderStatus     latest order status after the event
 * @param message         human-readable description shown to the user
 * @param timestamp       when the event occurred
 */
public record OrderNotificationPayload(
        UUID notificationId,
        UUID orderId,
        UUID customerId,
        NotificationType type,
        OrderStatus orderStatus,
        String message,
        LocalDateTime timestamp) {}
