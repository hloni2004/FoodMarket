package com.llburgers.repository;

import com.llburgers.domain.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    // ─── By Order ─────────────────────────────────────────────────────────────

    List<OrderItem> findByOrderId(UUID orderId);

    // ─── By Product ───────────────────────────────────────────────────────────

    List<OrderItem> findByProductId(UUID productId);

    long countByProductId(UUID productId);

    // ─── Analytics ────────────────────────────────────────────────────────────

    /** Sum of all order-item quantities ever sold across all products. */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi")
    Long sumTotalQuantitySold();

    /**
     * Most-ordered products ranked by total quantity sold.
     * Returns rows of [product_id, product_name, total_quantity, total_revenue].
     */
    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity), SUM(oi.totalPrice) " +
           "FROM OrderItem oi " +
           "GROUP BY oi.product.id, oi.product.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findPopularProducts(Pageable pageable);
}

