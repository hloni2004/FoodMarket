package com.llburgers.service.impl;

import com.llburgers.domain.Side;
import com.llburgers.repository.SideRepository;
import com.llburgers.service.ISideService;
import com.llburgers.service.IStorageService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SideServiceImpl implements ISideService {

    private final SideRepository repository;
    private final IStorageService storageService;

    public SideServiceImpl(SideRepository repository, IStorageService storageService) {
        this.repository = repository;
        this.storageService = storageService;
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @CacheEvict(value = "sides", allEntries = true)
    @Override
    public Side create(Side side) {
        return repository.save(side);
    }

    @Cacheable(value = "sides", key = "#id")
    @Override
    public Side read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Side not found with id: " + id));
    }

    @CacheEvict(value = "sides", allEntries = true)
    @Override
    public Side update(Side side) {
        if (side.getId() == null || !repository.existsById(side.getId())) {
            throw new IllegalArgumentException("Side not found with id: " + side.getId());
        }
        return repository.save(side);
    }

    @Cacheable(value = "sides", key = "'all'")
    @Override
    public List<Side> getAll() {
        return repository.findAll();
    }

    @CacheEvict(value = "sides", allEntries = true)
    @Override
    public void delete(UUID id) {
        Side side = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Side not found with id: " + id));
        // Remove image from Supabase Storage before deleting the record
        if (side.getImageUrl() != null) {
            storageService.deleteImage(side.getImageUrl());
        }
        repository.deleteById(id);
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @Override
    public List<Side> findByAvailability(boolean availability) {
        return repository.findByAvailability(availability);
    }

    @Override
    public List<Side> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return repository.findByPriceBetween(minPrice, maxPrice);
    }

    // ─── Stock Management ─────────────────────────────────────────────────────

    @Override
    public List<Side> findInStock() {
        return repository.findByStockQuantityGreaterThan(0);
    }

    @Override
    public List<Side> findOutOfStock() {
        return repository.findByStockQuantityEquals(0);
    }

    @CacheEvict(value = "sides", allEntries = true)
    @Override
    public Side updateStock(UUID id, int quantity) {
        Side side = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Side not found with id: " + id));
        side.setStockQuantity(quantity);
        // Auto-hide when out of stock, auto-show when restocked
        side.setAvailability(quantity > 0);
        return repository.save(side);
    }

    @CacheEvict(value = "sides", allEntries = true)
    @Override
    public Side reduceStock(UUID id, int quantity) {
        Side side = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Side not found with id: " + id));
        int newStock = side.getStockQuantity() - quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock for side: " + side.getName()
                    + " (available: " + side.getStockQuantity() + ", requested: " + quantity + ")");
        }
        side.setStockQuantity(newStock);
        // Auto-hide when stock hits zero
        if (newStock == 0) {
            side.setAvailability(false);
        }
        return repository.save(side);
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @CacheEvict(value = "sides", allEntries = true)
    @Override
    public Side markUnavailable(UUID id) {
        Side side = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Side not found with id: " + id));
        side.setAvailability(false);
        return repository.save(side);
    }

    @CacheEvict(value = "sides", allEntries = true)
    @Override
    public Side markAvailable(UUID id) {
        Side side = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Side not found with id: " + id));
        side.setAvailability(true);
        return repository.save(side);
    }

    // ─── Image Management (Supabase Storage) ──────────────────────────────────

    @CacheEvict(value = "sides", allEntries = true)
    @Override
    public Side uploadImage(UUID id, MultipartFile file) {
        Side side = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Side not found with id: " + id));
        // Delete old image if one already exists
        if (side.getImageUrl() != null) {
            storageService.deleteImage(side.getImageUrl());
        }
        String publicUrl = storageService.uploadImage(file, "sides");
        side.setImageUrl(publicUrl);
        return repository.save(side);
    }

    @CacheEvict(value = "sides", allEntries = true)
    @Override
    public Side removeImage(UUID id) {
        Side side = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Side not found with id: " + id));
        if (side.getImageUrl() != null) {
            storageService.deleteImage(side.getImageUrl());
            side.setImageUrl(null);
            return repository.save(side);
        }
        return side;
    }
}

