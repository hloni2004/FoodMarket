package com.llburgers.repository;

import com.llburgers.domain.Side;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface SideRepository extends JpaRepository<Side, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    boolean existsByName(String name);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Side> findByAvailability(boolean availability);

    List<Side> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Side> findByStockQuantityGreaterThan(int quantity);

    List<Side> findByStockQuantityEquals(int quantity);
}

