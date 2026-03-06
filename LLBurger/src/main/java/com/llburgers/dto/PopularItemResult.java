package com.llburgers.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Most-ordered products/extras/sides — used for the "popular items" chart.
 *
 * @param itemId            entity ID of the product/extra/side
 * @param itemName          display name
 * @param totalQuantityOrdered total units sold across all orders
 * @param totalRevenue      total revenue contributed
 */
public record PopularItemResult(UUID itemId, String itemName, long totalQuantityOrdered, BigDecimal totalRevenue) {}
