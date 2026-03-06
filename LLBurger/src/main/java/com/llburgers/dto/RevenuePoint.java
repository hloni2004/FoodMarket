package com.llburgers.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Revenue aggregated per calendar day.
 *
 * @param date         the calendar day
 * @param totalRevenue sum of order totals for that day
 * @param orderCount   number of orders placed that day
 */
public record RevenuePoint(LocalDate date, BigDecimal totalRevenue, long orderCount) {}
