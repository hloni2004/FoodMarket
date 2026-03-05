package com.llburgers.repository;

import com.llburgers.domain.OrderItemExtra;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

