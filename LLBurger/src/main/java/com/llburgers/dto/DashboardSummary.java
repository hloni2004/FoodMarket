package com.llburgers.dto;

import java.math.BigDecimal;

/**
 * High-level KPIs shown on the admin dashboard home page.
 *
 * @param totalRevenue         all-time total revenue
 * @param totalOrders          all-time number of orders
 * @param totalCustomers       total registered customers
 * @param averageOrderValue    average order total (all-time)
 * @param processingCount      orders currently in PROCESSING state
 * @param onTheWayCount        orders currently in ON_THE_WAY state
 * @param deliveredCount       orders in DELIVERED state
 * @param totalProductsSold    total product units sold (sum of order item quantities)
 */
public record DashboardSummary(
        BigDecimal totalRevenue,
        long totalOrders,
        long totalCustomers,
        BigDecimal averageOrderValue,
        long processingCount,
        long onTheWayCount,
        long deliveredCount,
        long totalProductsSold) {}
