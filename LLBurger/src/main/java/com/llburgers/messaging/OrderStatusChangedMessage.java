package com.llburgers.messaging;

import java.util.UUID;

/**
 * Lightweight message placed on {@code llburger.orders.status-changed} queue
 * when an admin advances an order through the status workflow.
 *
 * @param orderId        the order UUID whose status changed
 * @param previousStatus the status before the transition (e.g. "PROCESSING")
 * @param newStatus      the status after the transition (e.g. "ON_THE_WAY")
 */
public record OrderStatusChangedMessage(
        UUID orderId,
        String previousStatus,
        String newStatus) {}
