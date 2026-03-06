package com.llburgers.event;

import java.util.UUID;

/**
 * Published whenever stock changes for a product, extra, or side —
 * either through order placement (decrease) or order cancellation (restore).
 *
 * <p><b>Consumers:</b></p>
 * <ul>
 *     <li>Admin dashboards (real-time stock display)</li>
 *     <li>Analytics systems (popular item tracking)</li>
 * </ul>
 *
 * @param itemType  the category of item whose stock changed ("PRODUCT", "EXTRA", or "SIDE")
 * @param itemId    entity ID
 * @param itemName  display name for logging / dashboard
 * @param oldStock  stock quantity before the change
 * @param newStock  stock quantity after the change
 * @param delta     the signed change (negative = decrease, positive = restore)
 */
public record StockUpdatedEvent(
        String itemType,
        UUID itemId,
        String itemName,
        int oldStock,
        int newStock,
        int delta) {}
