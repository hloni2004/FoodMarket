package com.llburgers.controller;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatus;
import com.llburgers.domain.BusinessStatusLog;
import com.llburgers.service.IAdminService;
import com.llburgers.service.IBusinessStatusLogService;
import com.llburgers.service.IBusinessStatusService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/business-status")
@CrossOrigin
public class BusinessStatusController {

    private final IBusinessStatusService businessStatusService;
    private final IBusinessStatusLogService businessStatusLogService;
    private final IAdminService adminService;

    public BusinessStatusController(IBusinessStatusService businessStatusService,
                                    IBusinessStatusLogService businessStatusLogService,
                                    IAdminService adminService) {
        this.businessStatusService = businessStatusService;
        this.businessStatusLogService = businessStatusLogService;
        this.adminService = adminService;
    }

    // ─── Current Status ───────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<BusinessStatus> getCurrentStatus() {
        return ResponseEntity.ok(businessStatusService.getCurrentStatus());
    }

    @GetMapping("/is-open")
    public ResponseEntity<Boolean> isOpen() {
        return ResponseEntity.ok(businessStatusService.isOpen());
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @PostMapping("/open")
    public ResponseEntity<BusinessStatus> openBusiness(@RequestParam UUID adminId) {
        Admin admin = adminService.read(adminId);
        return ResponseEntity.ok(businessStatusService.openBusiness(admin));
    }

    @PostMapping("/close")
    public ResponseEntity<BusinessStatus> closeBusiness(
            @RequestParam UUID adminId,
            @RequestParam String closedMessage,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime expectedReopenAt) {
        Admin admin = adminService.read(adminId);
        return ResponseEntity.ok(businessStatusService.closeBusiness(admin, closedMessage, expectedReopenAt));
    }

    // ─── Logs ─────────────────────────────────────────────────────────────────

    @GetMapping("/logs")
    public ResponseEntity<List<BusinessStatusLog>> getAllLogs() {
        return ResponseEntity.ok(businessStatusLogService.getAll());
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<BusinessStatusLog> getLog(@PathVariable UUID id) {
        return ResponseEntity.ok(businessStatusLogService.read(id));
    }

    @GetMapping("/logs/admin/{adminId}")
    public ResponseEntity<List<BusinessStatusLog>> findLogsByAdmin(@PathVariable UUID adminId) {
        return ResponseEntity.ok(businessStatusLogService.findByAdminId(adminId));
    }

    @GetMapping("/logs/open")
    public ResponseEntity<List<BusinessStatusLog>> findLogsByOpen(@RequestParam(defaultValue = "true") boolean open) {
        return ResponseEntity.ok(businessStatusLogService.findByOpen(open));
    }

    @GetMapping("/logs/date-range")
    public ResponseEntity<List<BusinessStatusLog>> findLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(businessStatusLogService.findByDateRange(from, to));
    }

    @GetMapping("/logs/open/date-range")
    public ResponseEntity<List<BusinessStatusLog>> findLogsByOpenAndDateRange(
            @RequestParam boolean open,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(businessStatusLogService.findByOpenAndDateRange(open, from, to));
    }

    @GetMapping("/logs/recent")
    public ResponseEntity<BusinessStatusLog> findMostRecentLog() {
        return businessStatusLogService.findMostRecent()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
