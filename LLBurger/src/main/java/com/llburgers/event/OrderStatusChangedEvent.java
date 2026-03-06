package com.llburgers.event;

import com.llburgers.domain.Order;
import com.llburgers.domain.enums.OrderStatus;

/**
 * Published when an admin transitions an order's status through the
 * structured workflow: PROCESSING → ON_THE_WAY → DELIVERED.
 *
 * <p><b>Consumers:</b></p>
 * <ul>
 *     <li>{@code OrderNotificationListener} – sends status-update email (Mailjet)</li>
 *     <li>{@code InAppNotificationListener} – creates in-app message for PROCESSING state</li>
 * </ul>
 *
 * @param order         the order after the status change has been persisted
 * @param previousStatus the status before the transition
 * @param newStatus      the status after the transition
 */
public record OrderStatusChangedEvent(
        Order order,
        OrderStatus previousStatus,
        OrderStatus newStatus) {}
