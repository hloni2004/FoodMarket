package com.llburgers.service;

import com.llburgers.domain.OrderItem;

import java.util.List;
import java.util.UUID;

public interface IOrderItemService extends IService<OrderItem, UUID> {

    // ─── By Order ─────────────────────────────────────────────────────────────

    List<OrderItem> findByOrderId(UUID orderId);

    // ─── By Product ───────────────────────────────────────────────────────────

    List<OrderItem> findByProductId(UUID productId);

    long countByProductId(UUID productId);
}

