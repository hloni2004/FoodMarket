package com.llburgers.service;

import com.llburgers.domain.Order;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IOrderService extends IService<Order, UUID> {

    // ─── By Customer ─────────────────────────────────────────────────────────

    List<Order> findByCustomerId(UUID customerId);

    List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status);

    List<Order> findOrderHistory(UUID customerId);

    // ─── By Status ────────────────────────────────────────────────────────────

    List<Order> findByStatus(OrderStatus status);

    long countByStatus(OrderStatus status);

    // ─── By Delivery Location ─────────────────────────────────────────────────

    List<Order> findByDeliveryBlock(Block deliveryBlock);

    List<Order> findByDeliveryBlockAndStatus(Block deliveryBlock, OrderStatus status);

    // ─── By Date ──────────────────────────────────────────────────────────────

    List<Order> findByDateRange(LocalDateTime from, LocalDateTime to);

    List<Order> findByStatusAndDateRange(OrderStatus status, LocalDateTime from, LocalDateTime to);

    // ─── Business Logic ───────────────────────────────────────────────────────

    Order updateStatus(UUID id, OrderStatus status);

    Order cancel(UUID id);
}

