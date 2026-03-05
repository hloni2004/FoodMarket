package com.llburgers.service.impl;

import com.llburgers.domain.BusinessStatusLog;
import com.llburgers.repository.BusinessStatusLogRepository;
import com.llburgers.service.IBusinessStatusLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BusinessStatusLogServiceImpl implements IBusinessStatusLogService {

    private final BusinessStatusLogRepository repository;

    public BusinessStatusLogServiceImpl(BusinessStatusLogRepository repository) {
        this.repository = repository;
    }

    // ─── CRUD (from IService) ─────────────────────────────────────────────────

    @Override
    public BusinessStatusLog create(BusinessStatusLog log) {
        return repository.save(log);
    }

    @Override
    public BusinessStatusLog read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("BusinessStatusLog not found with id: " + id));
    }

    @Override
    public BusinessStatusLog update(BusinessStatusLog log) {
        if (log.getId() == null || !repository.existsById(log.getId())) {
            throw new IllegalArgumentException("BusinessStatusLog not found with id: " + log.getId());
        }
        return repository.save(log);
    }

    @Override
    public List<BusinessStatusLog> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("BusinessStatusLog not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ─── By Admin ─────────────────────────────────────────────────────────────

    @Override
    public List<BusinessStatusLog> findByAdminId(UUID adminId) {
        return repository.findByChangedById(adminId);
    }

    // ─── By State ─────────────────────────────────────────────────────────────

    @Override
    public List<BusinessStatusLog> findByOpen(boolean open) {
        return repository.findByOpen(open);
    }

    // ─── By Date ──────────────────────────────────────────────────────────────

    @Override
    public List<BusinessStatusLog> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return repository.findByChangedAtBetween(from, to);
    }

    @Override
    public List<BusinessStatusLog> findByOpenAndDateRange(boolean open, LocalDateTime from, LocalDateTime to) {
        return repository.findByOpenAndChangedAtBetween(open, from, to);
    }

    // ─── Most Recent ──────────────────────────────────────────────────────────

    @Override
    public Optional<BusinessStatusLog> findMostRecent() {
        return repository.findTopByOrderByChangedAtDesc();
    }
}

