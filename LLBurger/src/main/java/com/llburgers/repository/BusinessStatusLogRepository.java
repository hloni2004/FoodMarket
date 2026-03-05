package com.llburgers.repository;

import com.llburgers.domain.BusinessStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessStatusLogRepository extends JpaRepository<BusinessStatusLog, UUID> {

    // ─── By Admin ─────────────────────────────────────────────────────────────

    List<BusinessStatusLog> findByChangedById(UUID adminId);

    // ─── By State ─────────────────────────────────────────────────────────────

    List<BusinessStatusLog> findByOpen(boolean open);

    // ─── By Date ──────────────────────────────────────────────────────────────

    List<BusinessStatusLog> findByChangedAtBetween(LocalDateTime from, LocalDateTime to);

    List<BusinessStatusLog> findByOpenAndChangedAtBetween(boolean open, LocalDateTime from, LocalDateTime to);

    // ─── Most Recent ──────────────────────────────────────────────────────────

    Optional<BusinessStatusLog> findTopByOrderByChangedAtDesc();
}

