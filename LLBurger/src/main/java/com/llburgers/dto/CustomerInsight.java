package com.llburgers.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Per-customer ordering behaviour for the customer insights report.
 *
 * @param customerId        customer entity ID
 * @param customerName      customer display name
 * @param email             customer email
 * @param orderCount        total number of completed orders
 * @param totalSpent        total amount spent (sum of order totals)
 * @param averageOrderValue average value per order
 */
public record CustomerInsight(
        UUID customerId,
        String customerName,
        String email,
        long orderCount,
        BigDecimal totalSpent,
        BigDecimal averageOrderValue) {}
