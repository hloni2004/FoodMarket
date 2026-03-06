package com.llburgers.repository;

import com.llburgers.domain.Order;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // ─── By Customer ─────────────────────────────────────────────────────────

    List<Order> findByCustomerId(UUID customerId);

    List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status);

    List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    // ─── By Status ────────────────────────────────────────────────────────────

    List<Order> findByStatus(OrderStatus status);

    long countByStatus(OrderStatus status);

    // ─── By Delivery Location ─────────────────────────────────────────────────

    List<Order> findByDeliveryBlock(Block deliveryBlock);

    List<Order> findByDeliveryBlockAndStatus(Block deliveryBlock, OrderStatus status);

    // ─── By Date ──────────────────────────────────────────────────────────────

    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime from, LocalDateTime to);

    // ─── Analytics ────────────────────────────────────────────────────────────

    /** All-time total revenue across all orders. */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o")
    BigDecimal sumTotalRevenue();

    /** Total revenue within a date range. */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    BigDecimal sumRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Revenue aggregated per calendar day within range.
     * Returns rows of [date_string (text), total_price (numeric), order_count (bigint)].
     */
    @Query(value = """
            SELECT CAST(o.created_at AS DATE)  AS rev_date,
                   SUM(o.total_price)           AS total_revenue,
                   COUNT(o.id)                  AS order_count
            FROM   orders o
            WHERE  o.created_at BETWEEN :from AND :to
            GROUP  BY CAST(o.created_at AS DATE)
            ORDER  BY CAST(o.created_at AS DATE)
            """, nativeQuery = true)
    List<Object[]> findRevenueByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * All-time order volume and revenue aggregated by hour-of-day (0–23).
     * Returns rows of [hour (double), order_count (bigint), total_revenue (numeric)].
     */
    @Query(value = """
            SELECT EXTRACT(HOUR FROM o.created_at) AS hour,
                   COUNT(o.id)                      AS order_count,
                   SUM(o.total_price)               AS total_revenue
            FROM   orders o
            GROUP  BY EXTRACT(HOUR FROM o.created_at)
            ORDER  BY EXTRACT(HOUR FROM o.created_at)
            """, nativeQuery = true)
    List<Object[]> findPeakHours();

    /**
     * Order volume and revenue aggregated by hour-of-day within a date range.
     * Returns rows of [hour (double), order_count (bigint), total_revenue (numeric)].
     */
    @Query(value = """
            SELECT EXTRACT(HOUR FROM o.created_at) AS hour,
                   COUNT(o.id)                      AS order_count,
                   SUM(o.total_price)               AS total_revenue
            FROM   orders o
            WHERE  o.created_at BETWEEN :from AND :to
            GROUP  BY EXTRACT(HOUR FROM o.created_at)
            ORDER  BY EXTRACT(HOUR FROM o.created_at)
            """, nativeQuery = true)
    List<Object[]> findPeakHoursInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Top customers ranked by total spend.
     * Returns rows of [customer_id (uuid), name (text), email (text), order_count (bigint), total_spent (numeric)].
     */
    @Query("SELECT o.customer.id, o.customer.name, o.customer.email, COUNT(o.id), SUM(o.totalPrice) " +
           "FROM Order o " +
           "GROUP BY o.customer.id, o.customer.name, o.customer.email " +
           "ORDER BY SUM(o.totalPrice) DESC")
    List<Object[]> findTopCustomers(Pageable pageable);
}

