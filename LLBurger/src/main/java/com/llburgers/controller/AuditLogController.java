package com.llburgers.controller;

import com.llburgers.domain.AuditLog;
import com.llburgers.service.IAuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final IAuditLogService auditLogService;

    public AuditLogController(IAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<List<AuditLog>> getAll() {
        return ResponseEntity.ok(auditLogService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER')")
    public ResponseEntity<AuditLog> create(@RequestBody Map<String, String> body) {
        AuditLog entry = auditLogService.log(
                body.getOrDefault("action", "unknown"),
                body.getOrDefault("entity", "unknown"),
                body.getOrDefault("detail", ""),
                body.getOrDefault("performedBy", "admin")
        );
        return ResponseEntity.ok(entry);
    }
}
