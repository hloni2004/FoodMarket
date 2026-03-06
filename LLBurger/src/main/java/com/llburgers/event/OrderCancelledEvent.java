package com.llburgers.event;

import com.llburgers.domain.Order;

/**
 * Published when an order is cancelled and its stock is restored.
 *
 * <p><b>Consumers:</b></p>
 * <ul>
 *     <li>{@code StockEventListener} – fires {@link StockUpdatedEvent} to notify dashboards</li>
 * </ul>
 *
 * @param order the order that was cancelled (still contains items for reference)
 */
public record OrderCancelledEvent(Order order) {}
