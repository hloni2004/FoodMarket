package com.llburgers.dto;

import java.math.BigDecimal;

/**
 * Order volume and revenue aggregated by hour-of-day (0–23).
 * Used to identify peak trading hours.
 *
 * @param hour         hour of day (0 = midnight, 12 = noon, etc.)
 * @param orderCount   number of orders placed in that hour
 * @param totalRevenue total revenue generated in that hour
 */
public record PeakHourPoint(int hour, long orderCount, BigDecimal totalRevenue) {}
