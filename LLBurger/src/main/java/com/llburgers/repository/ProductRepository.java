package com.llburgers.repository;

import com.llburgers.domain.Product;
import com.llburgers.domain.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    boolean existsByName(String name);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByAvailability(boolean availability);

    List<Product> findByCategoryAndAvailability(ProductCategory category, boolean availability);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByStockQuantityGreaterThan(int quantity);

    List<Product> findByStockQuantityEquals(int quantity);
}

