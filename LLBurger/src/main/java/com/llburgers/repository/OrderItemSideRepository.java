package com.llburgers.repository;

import com.llburgers.domain.OrderItemSide;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

