package com.llburgers.service;

import com.llburgers.domain.Extra;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IExtraService extends IService<Extra, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    boolean existsByName(String name);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Extra> findByAvailability(boolean availability);

    List<Extra> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // ─── Stock Management ─────────────────────────────────────────────────────

    List<Extra> findInStock();

    List<Extra> findOutOfStock();

    Extra updateStock(UUID id, int quantity);

    Extra reduceStock(UUID id, int quantity);

    // ─── Business Logic ───────────────────────────────────────────────────────

    Extra markUnavailable(UUID id);

    Extra markAvailable(UUID id);

    // ─── Image Management (Supabase Storage) ──────────────────────────────────

    Extra uploadImage(UUID id, MultipartFile file);

    Extra removeImage(UUID id);
}

