package com.llburgers.service;

import com.llburgers.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Analytics service providing sales dashboard data, peak hour analysis,
 * popular item rankings, and customer insights as per the system specification.
 */
public interface IAnalyticsService {

    // ─── Dashboard KPIs ───────────────────────────────────────────────────────

    /**
     * Returns a high-level snapshot of all key performance indicators:
     * total revenue, order counts by status, customer count, and units sold.
     */
    DashboardSummary getDashboardSummary();

    // ─── Revenue Trends ───────────────────────────────────────────────────────

    /**
     * Daily revenue totals within the supplied inclusive date range.
     * Days with no orders are omitted from the result.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return list of {@link RevenuePoint} ordered by date ascending
     */
    List<RevenuePoint> getRevenueByDay(LocalDate from, LocalDate to);

    /**
     * Total revenue for the given date range.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     */
    BigDecimal getTotalRevenue(LocalDate from, LocalDate to);

    // ─── Peak Hours ───────────────────────────────────────────────────────────

    /**
     * All-time order volume and revenue grouped by hour of day (0–23).
     *
     * @return 24 potential entries (hours with no orders are omitted)
     */
    List<PeakHourPoint> getPeakHours();

    /**
     * Order volume and revenue grouped by hour of day within a date range.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     */
    List<PeakHourPoint> getPeakHours(LocalDate from, LocalDate to);

    // ─── Popular Items ────────────────────────────────────────────────────────

    /**
     * Top products ranked by total quantity ordered.
     *
     * @param limit maximum number of results to return
     */
    List<PopularItemResult> getPopularProducts(int limit);

    /**
     * Top extras ranked by total quantity ordered.
     *
     * @param limit maximum number of results to return
     */
    List<PopularItemResult> getPopularExtras(int limit);

    /**
     * Top sides ranked by total quantity ordered.
     *
     * @param limit maximum number of results to return
     */
    List<PopularItemResult> getPopularSides(int limit);

    // ─── Customer Insights ────────────────────────────────────────────────────

    /**
     * Top customers ranked by total amount spent.
     *
     * @param limit maximum number of results to return
     */
    List<CustomerInsight> getTopCustomers(int limit);
}
