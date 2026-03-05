package com.llburgers.service.impl;

import com.llburgers.domain.Extra;
import com.llburgers.repository.ExtraRepository;
import com.llburgers.service.IExtraService;
import com.llburgers.service.IStorageService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ExtraServiceImpl implements IExtraService {

    private final ExtraRepository repository;
    private final IStorageService storageService;

    public ExtraServiceImpl(ExtraRepository repository, IStorageService storageService) {
        this.repository = repository;
        this.storageService = storageService;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @CacheEvict(value = "extras", allEntries = true)
    @Override
    public Extra create(Extra extra) {
        return repository.save(extra);
    }

    @Cacheable(value = "extras", key = "#id")
    @Override
    public Extra read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Extra not found with id: " + id));
    }

    @CacheEvict(value = "extras", allEntries = true)
    @Override
    public Extra update(Extra extra) {
        if (extra.getId() == null || !repository.existsById(extra.getId())) {
            throw new IllegalArgumentException("Extra not found with id: " + extra.getId());
        }
        return repository.save(extra);
    }

    @Cacheable(value = "extras", key = "'all'")
    @Override
    public List<Extra> getAll() {
        return repository.findAll();
    }

    @CacheEvict(value = "extras", allEntries = true)
    @Override
    public void delete(UUID id) {
        Extra extra = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Extra not found with id: " + id));
        // Remove image from Supabase Storage before deleting the record
        if (extra.getImageUrl() != null) {
            storageService.deleteImage(extra.getImageUrl());
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
    public List<Extra> findByAvailability(boolean availability) {
        return repository.findByAvailability(availability);
    }

    @Override
    public List<Extra> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return repository.findByPriceBetween(minPrice, maxPrice);
    }

    // ─── Stock Management ─────────────────────────────────────────────────────

    @Override
    public List<Extra> findInStock() {
        return repository.findByStockQuantityGreaterThan(0);
    }

    @Override
    public List<Extra> findOutOfStock() {
        return repository.findByStockQuantityEquals(0);
    }

    @CacheEvict(value = "extras", allEntries = true)
    @Override
    public Extra updateStock(UUID id, int quantity) {
        Extra extra = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Extra not found with id: " + id));
        extra.setStockQuantity(quantity);
        // Auto-hide when out of stock, auto-show when restocked
        extra.setAvailability(quantity > 0);
        return repository.save(extra);
    }

    @CacheEvict(value = "extras", allEntries = true)
    @Override
    public Extra reduceStock(UUID id, int quantity) {
        Extra extra = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Extra not found with id: " + id));
        int newStock = extra.getStockQuantity() - quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock for extra: " + extra.getName()
                    + " (available: " + extra.getStockQuantity() + ", requested: " + quantity + ")");
        }
        extra.setStockQuantity(newStock);
        // Auto-hide when stock hits zero
        if (newStock == 0) {
            extra.setAvailability(false);
        }
        return repository.save(extra);
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @CacheEvict(value = "extras", allEntries = true)
    @Override
    public Extra markUnavailable(UUID id) {
        Extra extra = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Extra not found with id: " + id));
        extra.setAvailability(false);
        return repository.save(extra);
    }

    @CacheEvict(value = "extras", allEntries = true)
    @Override
    public Extra markAvailable(UUID id) {
        Extra extra = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Extra not found with id: " + id));
        extra.setAvailability(true);
        return repository.save(extra);
    }

    // ─── Image Management (Supabase Storage) ──────────────────────────────────

    @CacheEvict(value = "extras", allEntries = true)
    @Override
    public Extra uploadImage(UUID id, MultipartFile file) {
        Extra extra = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Extra not found with id: " + id));
        // Delete old image if one already exists
        if (extra.getImageUrl() != null) {
            storageService.deleteImage(extra.getImageUrl());
        }
        String publicUrl = storageService.uploadImage(file, "extras");
        extra.setImageUrl(publicUrl);
        return repository.save(extra);
    }

    @CacheEvict(value = "extras", allEntries = true)
    @Override
    public Extra removeImage(UUID id) {
        Extra extra = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Extra not found with id: " + id));
        if (extra.getImageUrl() != null) {
            storageService.deleteImage(extra.getImageUrl());
            extra.setImageUrl(null);
            return repository.save(extra);
        }
        return extra;
    }
}
