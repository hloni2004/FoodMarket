package com.llburgers.service.impl;

import com.llburgers.domain.enums.OrderStatus;
import com.llburgers.dto.*;
import com.llburgers.repository.*;
import com.llburgers.service.IAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Analytics service implementation.
 *
 * <p>All results that are expensive to compute are cached via Redis (cache names
 * match the method names). Callers should invalidate or use short TTLs when
 * real-time accuracy matters.</p>
 *
 * <p>All queries are read-only ({@code @Transactional(readOnly = true)}) to
 * allow the JPA provider to skip dirty-checking overhead and, when a read
 * replica is configured, to route queries there automatically.</p>
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements IAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemExtraRepository orderItemExtraRepository;
    private final OrderItemSideRepository orderItemSideRepository;
    private final CustomerRepository customerRepository;

    public AnalyticsServiceImpl(OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                OrderItemExtraRepository orderItemExtraRepository,
                                OrderItemSideRepository orderItemSideRepository,
                                CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderItemExtraRepository = orderItemExtraRepository;
        this.orderItemSideRepository = orderItemSideRepository;
        this.customerRepository = customerRepository;
    }

    // ─── Dashboard KPIs ───────────────────────────────────────────────────────

    @Override
    @Cacheable("dashboardSummary")
    public DashboardSummary getDashboardSummary() {
        log.debug("[ANALYTICS] Computing dashboard summary");

        BigDecimal totalRevenue = nullSafe(orderRepository.sumTotalRevenue());
        long totalOrders       = orderRepository.count();
        long totalCustomers    = customerRepository.count();

        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long processingCount = orderRepository.countByStatus(OrderStatus.PROCESSING);
        long onTheWayCount   = orderRepository.countByStatus(OrderStatus.ON_THE_WAY);
        long deliveredCount  = orderRepository.countByStatus(OrderStatus.DELIVERED);

        Long rawQty = orderItemRepository.sumTotalQuantitySold();
        long totalProductsSold = rawQty != null ? rawQty : 0L;

        return new DashboardSummary(
                totalRevenue,
                totalOrders,
                totalCustomers,
                averageOrderValue,
                processingCount,
                onTheWayCount,
                deliveredCount,
                totalProductsSold);
    }

    // ─── Revenue Trends ───────────────────────────────────────────────────────

    @Override
    @Cacheable("revenueByDay")
    public List<RevenuePoint> getRevenueByDay(LocalDate from, LocalDate to) {
        log.debug("[ANALYTICS] Revenue by day: {} → {}", from, to);

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.atTime(LocalTime.MAX);

        return orderRepository.findRevenueByDay(start, end)
                .stream()
                .map(row -> new RevenuePoint(
                        toLocalDate(row[0]),
                        toBigDecimal(row[1]),
                        toLong(row[2])))
                .toList();
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.atTime(LocalTime.MAX);
        return nullSafe(orderRepository.sumRevenueBetween(start, end));
    }

    // ─── Peak Hours ───────────────────────────────────────────────────────────

    @Override
    @Cacheable("peakHours")
    public List<PeakHourPoint> getPeakHours() {
        log.debug("[ANALYTICS] Peak hours (all-time)");
        return orderRepository.findPeakHours()
                .stream()
                .map(row -> new PeakHourPoint(toInt(row[0]), toLong(row[1]), toBigDecimal(row[2])))
                .toList();
    }

    @Override
    @Cacheable("peakHoursRange")
    public List<PeakHourPoint> getPeakHours(LocalDate from, LocalDate to) {
        log.debug("[ANALYTICS] Peak hours: {} → {}", from, to);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.atTime(LocalTime.MAX);
        return orderRepository.findPeakHoursInRange(start, end)
                .stream()
                .map(row -> new PeakHourPoint(toInt(row[0]), toLong(row[1]), toBigDecimal(row[2])))
                .toList();
    }

    // ─── Popular Items ────────────────────────────────────────────────────────

    @Override
    @Cacheable("popularProducts")
    public List<PopularItemResult> getPopularProducts(int limit) {
        log.debug("[ANALYTICS] Popular products (top {})", limit);
        Pageable page = PageRequest.of(0, limit);
        return orderItemRepository.findPopularProducts(page)
                .stream()
                .map(row -> new PopularItemResult(
                        toUUID(row[0]),
                        (String) row[1],
                        toLong(row[2]),
                        toBigDecimal(row[3])))
                .toList();
    }

    @Override
    @Cacheable("popularExtras")
    public List<PopularItemResult> getPopularExtras(int limit) {
        log.debug("[ANALYTICS] Popular extras (top {})", limit);
        Pageable page = PageRequest.of(0, limit);
        return orderItemExtraRepository.findPopularExtras(page)
                .stream()
                .map(row -> new PopularItemResult(
                        toUUID(row[0]),
                        (String) row[1],
                        toLong(row[2]),
                        toBigDecimal(row[3])))
                .toList();
    }

    @Override
    @Cacheable("popularSides")
    public List<PopularItemResult> getPopularSides(int limit) {
        log.debug("[ANALYTICS] Popular sides (top {})", limit);
        Pageable page = PageRequest.of(0, limit);
        return orderItemSideRepository.findPopularSides(page)
                .stream()
                .map(row -> new PopularItemResult(
                        toUUID(row[0]),
                        (String) row[1],
                        toLong(row[2]),
                        toBigDecimal(row[3])))
                .toList();
    }

    // ─── Customer Insights ────────────────────────────────────────────────────

    @Override
    @Cacheable("topCustomers")
    public List<CustomerInsight> getTopCustomers(int limit) {
        log.debug("[ANALYTICS] Top {} customers by spend", limit);
        Pageable page = PageRequest.of(0, limit);
        return orderRepository.findTopCustomers(page)
                .stream()
                .map(row -> {
                    BigDecimal totalSpent = toBigDecimal(row[4]);
                    long orderCount       = toLong(row[3]);
                    BigDecimal avgOrder   = orderCount > 0
                            ? totalSpent.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return new CustomerInsight(
                            toUUID(row[0]),
                            (String) row[1],
                            (String) row[2],
                            orderCount,
                            totalSpent,
                            avgOrder);
                })
                .toList();
    }

    // ─── Mapping helpers ──────────────────────────────────────────────────────

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(value.toString());
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(value.toString());
    }

    private UUID toUUID(Object value) {
        if (value instanceof UUID u) return u;
        return UUID.fromString(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof java.sql.Date sd) return sd.toLocalDate();
        return LocalDate.parse(value.toString());
    }
}
