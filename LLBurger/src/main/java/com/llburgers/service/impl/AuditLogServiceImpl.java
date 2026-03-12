package com.llburgers.service.impl;

import com.llburgers.domain.AuditLog;
import com.llburgers.repository.AuditLogRepository;
import com.llburgers.service.IAuditLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements IAuditLogService {

    private final AuditLogRepository repository;

    public AuditLogServiceImpl(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditLog log(String action, String entity, String detail, String performedBy) {
        AuditLog entry = AuditLog.builder()
                .action(action)
                .entity(entity)
                .detail(detail)
                .performedBy(performedBy)
                .build();
        return repository.save(entry);
    }

    @Override
    public List<AuditLog> getAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
