package com.llburgers.repository;

import com.llburgers.domain.Order;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}

