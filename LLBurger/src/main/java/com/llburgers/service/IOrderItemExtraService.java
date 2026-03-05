package com.llburgers.service;

import com.llburgers.domain.OrderItemExtra;

import java.util.List;
import java.util.UUID;

public interface IOrderItemExtraService extends IService<OrderItemExtra, UUID> {

    // ─── By Order Item ────────────────────────────────────────────────────────

    List<OrderItemExtra> findByOrderItemId(UUID orderItemId);

    // ─── By Extra ─────────────────────────────────────────────────────────────

    List<OrderItemExtra> findByExtraId(UUID extraId);

    long countByExtraId(UUID extraId);
}

