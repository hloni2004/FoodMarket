package com.llburgers.service.impl;

import com.llburgers.domain.OrderItemExtra;
import com.llburgers.repository.OrderItemExtraRepository;
import com.llburgers.service.IOrderItemExtraService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderItemExtraServiceImpl implements IOrderItemExtraService {

    private final OrderItemExtraRepository repository;

    public OrderItemExtraServiceImpl(OrderItemExtraRepository repository) {
        this.repository = repository;
    }

    // ─── CRUD (from IService) ─────────────────────────────────────────────────

    @Override
    public OrderItemExtra create(OrderItemExtra orderItemExtra) {
        return repository.save(orderItemExtra);
    }

    @Override
    public OrderItemExtra read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("OrderItemExtra not found with id: " + id));
    }

    @Override
    public OrderItemExtra update(OrderItemExtra orderItemExtra) {
        if (orderItemExtra.getId() == null || !repository.existsById(orderItemExtra.getId())) {
            throw new IllegalArgumentException("OrderItemExtra not found with id: " + orderItemExtra.getId());
        }
        return repository.save(orderItemExtra);
    }

    @Override
    public List<OrderItemExtra> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("OrderItemExtra not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ─── By Order Item ────────────────────────────────────────────────────────

    @Override
    public List<OrderItemExtra> findByOrderItemId(UUID orderItemId) {
        return repository.findByOrderItemId(orderItemId);
    }

    // ─── By Extra ─────────────────────────────────────────────────────────────

    @Override
    public List<OrderItemExtra> findByExtraId(UUID extraId) {
        return repository.findByExtraId(extraId);
    }

    @Override
    public long countByExtraId(UUID extraId) {
        return repository.countByExtraId(extraId);
    }
}

