package com.llburgers.service;

import com.llburgers.domain.BusinessStatusLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IBusinessStatusLogService extends IService<BusinessStatusLog, UUID> {

    // ─── By Admin ─────────────────────────────────────────────────────────────

    List<BusinessStatusLog> findByAdminId(UUID adminId);

    // ─── By State ─────────────────────────────────────────────────────────────

    List<BusinessStatusLog> findByOpen(boolean open);

    // ─── By Date ──────────────────────────────────────────────────────────────

    List<BusinessStatusLog> findByDateRange(LocalDateTime from, LocalDateTime to);

    List<BusinessStatusLog> findByOpenAndDateRange(boolean open, LocalDateTime from, LocalDateTime to);

    // ─── Most Recent ──────────────────────────────────────────────────────────

    Optional<BusinessStatusLog> findMostRecent();
}

