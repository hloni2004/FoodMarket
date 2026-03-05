package com.llburgers.repository;

import com.llburgers.domain.Extra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExtraRepository extends JpaRepository<Extra, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    boolean existsByName(String name);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Extra> findByAvailability(boolean availability);

    List<Extra> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Extra> findByStockQuantityGreaterThan(int quantity);

    List<Extra> findByStockQuantityEquals(int quantity);
}

