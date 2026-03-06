package com.llburgers.controller;

import com.llburgers.domain.Admin;
import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.service.IAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin
public class AdminController {

    private final IAdminService adminService;

    public AdminController(IAdminService adminService) {
        this.adminService = adminService;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Admin> create(@RequestBody Admin admin) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.create(admin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> read(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.read(id));
    }

    @PutMapping
    public ResponseEntity<Admin> update(@RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.update(admin));
    }

    @GetMapping
    public ResponseEntity<List<Admin>> getAll() {
        return ResponseEntity.ok(adminService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @GetMapping("/email/{email}")
    public ResponseEntity<Admin> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(adminService.findByEmail(email));
    }

    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(adminService.existsByEmail(email));
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @GetMapping("/level/{level}")
    public ResponseEntity<List<Admin>> findByAdminLevel(@PathVariable AdminLevel level) {
        return ResponseEntity.ok(adminService.findByAdminLevel(level));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Admin>> findByActive(@RequestParam(defaultValue = "true") boolean active) {
        return ResponseEntity.ok(adminService.findByActive(active));
    }

    @GetMapping("/level/{level}/active")
    public ResponseEntity<List<Admin>> findByAdminLevelAndActive(
            @PathVariable AdminLevel level, @RequestParam(defaultValue = "true") boolean active) {
        return ResponseEntity.ok(adminService.findByAdminLevelAndActive(level, active));
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Admin> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Admin> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.activate(id));
    }

    @PatchMapping("/{id}/level")
    public ResponseEntity<Admin> updateAdminLevel(
            @PathVariable UUID id, @RequestParam AdminLevel level) {
        return ResponseEntity.ok(adminService.updateAdminLevel(id, level));
    }
}
