package com.llburgers.repository;

import com.llburgers.domain.OrderItemSide;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemSideRepository extends JpaRepository<OrderItemSide, UUID> {

    // ─── By Order Item ────────────────────────────────────────────────────────

    List<OrderItemSide> findByOrderItemId(UUID orderItemId);

    // ─── By Side ──────────────────────────────────────────────────────────────

    List<OrderItemSide> findBySideId(UUID sideId);

    long countBySideId(UUID sideId);

    // ─── Analytics ────────────────────────────────────────────────────────────

    /**
     * Most-ordered sides ranked by total quantity.
     * Returns rows of [side_id, side_name, total_quantity, total_revenue].
     */
    @Query("SELECT ois.side.id, ois.side.name, SUM(ois.quantity), SUM(ois.quantity * ois.side.price) " +
           "FROM OrderItemSide ois " +
           "GROUP BY ois.side.id, ois.side.name " +
           "ORDER BY SUM(ois.quantity) DESC")
    List<Object[]> findPopularSides(Pageable pageable);
}

