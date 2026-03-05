package com.llburgers.service.impl;

import com.llburgers.domain.OrderItem;
import com.llburgers.repository.OrderItemRepository;
import com.llburgers.service.IOrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderItemServiceImpl implements IOrderItemService {

    private final OrderItemRepository repository;

    public OrderItemServiceImpl(OrderItemRepository repository) {
        this.repository = repository;
    }

    // ─── CRUD (from IService) ─────────────────────────────────────────────────

    @Override
    public OrderItem create(OrderItem orderItem) {
        return repository.save(orderItem);
    }

    @Override
    public OrderItem read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("OrderItem not found with id: " + id));
    }

    @Override
    public OrderItem update(OrderItem orderItem) {
        if (orderItem.getId() == null || !repository.existsById(orderItem.getId())) {
            throw new IllegalArgumentException("OrderItem not found with id: " + orderItem.getId());
        }
        return repository.save(orderItem);
    }

    @Override
    public List<OrderItem> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("OrderItem not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ─── By Order ─────────────────────────────────────────────────────────────

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId);
    }

    // ─── By Product ───────────────────────────────────────────────────────────

    @Override
    public List<OrderItem> findByProductId(UUID productId) {
        return repository.findByProductId(productId);
    }

    @Override
    public long countByProductId(UUID productId) {
        return repository.countByProductId(productId);
    }
}

