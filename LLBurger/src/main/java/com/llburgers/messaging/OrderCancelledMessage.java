package com.llburgers.messaging;

import java.util.UUID;

/**
 * Lightweight message placed on {@code llburger.orders.cancelled} queue
 * when a customer cancels their order and stock is restored.
 *
 * @param orderId the UUID of the cancelled order
 */
public record OrderCancelledMessage(UUID orderId) {}
