package com.llburgers.repository;

import com.llburgers.domain.OrderItemExtra;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemExtraRepository extends JpaRepository<OrderItemExtra, UUID> {

    // ─── By Order Item ────────────────────────────────────────────────────────

    List<OrderItemExtra> findByOrderItemId(UUID orderItemId);

    // ─── By Extra ─────────────────────────────────────────────────────────────

    List<OrderItemExtra> findByExtraId(UUID extraId);

    long countByExtraId(UUID extraId);

    // ─── Analytics ────────────────────────────────────────────────────────────

    /**
     * Most-ordered extras ranked by total quantity.
     * Returns rows of [extra_id, extra_name, total_quantity, total_revenue].
     */
    @Query("SELECT oie.extra.id, oie.extra.name, SUM(oie.quantity), SUM(oie.quantity * oie.extra.price) " +
           "FROM OrderItemExtra oie " +
           "GROUP BY oie.extra.id, oie.extra.name " +
           "ORDER BY SUM(oie.quantity) DESC")
    List<Object[]> findPopularExtras(Pageable pageable);
}

