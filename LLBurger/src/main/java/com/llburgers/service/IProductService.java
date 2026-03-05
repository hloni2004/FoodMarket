package com.llburgers.service;

import com.llburgers.domain.Product;
import com.llburgers.domain.enums.ProductCategory;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IProductService extends IService<Product, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    boolean existsByName(String name);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByAvailability(boolean availability);

    List<Product> findByCategoryAndAvailability(ProductCategory category, boolean availability);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // ─── Stock Management ─────────────────────────────────────────────────────

    List<Product> findInStock();

    List<Product> findOutOfStock();

    Product updateStock(UUID id, int quantity);

    Product reduceStock(UUID id, int quantity);

    // ─── Business Logic ───────────────────────────────────────────────────────

    Product markUnavailable(UUID id);

    Product markAvailable(UUID id);

    // ─── Image Management (Supabase Storage) ──────────────────────────────────

    Product uploadImage(UUID id, MultipartFile file);

    Product removeImage(UUID id);
}

