package com.llburgers.event;

import com.llburgers.domain.Order;

import java.util.List;
import java.util.UUID;

/**
 * Published after a new order has been validated, stock reduced, and persisted.
 *
 * <p><b>Consumers:</b></p>
 * <ul>
 *     <li>{@code OrderNotificationListener} – sends order-confirmation email (Mailjet)</li>
 *     <li>{@code StockEventListener} – fires {@link StockUpdatedEvent} per product/extra/side</li>
 * </ul>
 *
 * @param order             the fully saved order (with items, extras, sides populated)
 * @param affectedProductIds product IDs whose stock was reduced
 * @param affectedExtraIds   extra IDs whose stock was reduced
 * @param affectedSideIds    side IDs whose stock was reduced
 */
public record OrderPlacedEvent(
        Order order,
        List<UUID> affectedProductIds,
        List<UUID> affectedExtraIds,
        List<UUID> affectedSideIds) {}
