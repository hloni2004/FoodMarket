package com.llburgers.service;

import com.llburgers.domain.Side;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ISideService extends IService<Side, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    boolean existsByName(String name);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Side> findByAvailability(boolean availability);

    List<Side> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // ─── Stock Management ─────────────────────────────────────────────────────

    List<Side> findInStock();

    List<Side> findOutOfStock();

    Side updateStock(UUID id, int quantity);

    Side reduceStock(UUID id, int quantity);

    // ─── Business Logic ───────────────────────────────────────────────────────

    Side markUnavailable(UUID id);

    Side markAvailable(UUID id);

    // ─── Image Management (Supabase Storage) ──────────────────────────────────

    Side uploadImage(UUID id, MultipartFile file);

    Side removeImage(UUID id);
}

