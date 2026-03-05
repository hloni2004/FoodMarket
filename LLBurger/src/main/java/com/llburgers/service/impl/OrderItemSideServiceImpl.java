package com.llburgers.service.impl;

import com.llburgers.domain.OrderItemSide;
import com.llburgers.repository.OrderItemSideRepository;
import com.llburgers.service.IOrderItemSideService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderItemSideServiceImpl implements IOrderItemSideService {

    private final OrderItemSideRepository repository;

    public OrderItemSideServiceImpl(OrderItemSideRepository repository) {
        this.repository = repository;
    }

    // ─── CRUD (from IService) ─────────────────────────────────────────────────

    @Override
    public OrderItemSide create(OrderItemSide orderItemSide) {
        return repository.save(orderItemSide);
    }

    @Override
    public OrderItemSide read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("OrderItemSide not found with id: " + id));
    }

    @Override
    public OrderItemSide update(OrderItemSide orderItemSide) {
        if (orderItemSide.getId() == null || !repository.existsById(orderItemSide.getId())) {
            throw new IllegalArgumentException("OrderItemSide not found with id: " + orderItemSide.getId());
        }
        return repository.save(orderItemSide);
    }

    @Override
    public List<OrderItemSide> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("OrderItemSide not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ─── By Order Item ────────────────────────────────────────────────────────

    @Override
    public List<OrderItemSide> findByOrderItemId(UUID orderItemId) {
        return repository.findByOrderItemId(orderItemId);
    }

    // ─── By Side ──────────────────────────────────────────────────────────────

    @Override
    public List<OrderItemSide> findBySideId(UUID sideId) {
        return repository.findBySideId(sideId);
    }

    @Override
    public long countBySideId(UUID sideId) {
        return repository.countBySideId(sideId);
    }
}

