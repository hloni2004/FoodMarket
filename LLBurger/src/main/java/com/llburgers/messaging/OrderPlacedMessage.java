package com.llburgers.messaging;

import java.util.List;
import java.util.UUID;

/**
 * Lightweight message placed on {@code llburger.orders.placed} queue
 * when a new order is saved and stock is reduced.
 *
 * <p>Carries only IDs – the consumer fetches full domain objects from
 * the database, ensuring it works on the latest persisted state.</p>
 *
 * @param orderId            the persisted order UUID
 * @param affectedProductIds product IDs whose stock was reduced
 * @param affectedExtraIds   extra IDs whose stock was reduced
 * @param affectedSideIds    side IDs whose stock was reduced
 */
public record OrderPlacedMessage(
        UUID orderId,
        List<UUID> affectedProductIds,
        List<UUID> affectedExtraIds,
        List<UUID> affectedSideIds) {}
