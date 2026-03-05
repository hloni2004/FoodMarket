package com.llburgers.service;

import com.llburgers.domain.OrderItemSide;

import java.util.List;
import java.util.UUID;

public interface IOrderItemSideService extends IService<OrderItemSide, UUID> {

    // ─── By Order Item ────────────────────────────────────────────────────────

    List<OrderItemSide> findByOrderItemId(UUID orderItemId);

    // ─── By Side ──────────────────────────────────────────────────────────────

    List<OrderItemSide> findBySideId(UUID sideId);

    long countBySideId(UUID sideId);
}

