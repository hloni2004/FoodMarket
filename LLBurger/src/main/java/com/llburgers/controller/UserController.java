package com.llburgers.controller;

import com.llburgers.domain.User;
import com.llburgers.domain.enums.Role;
import com.llburgers.service.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<User> read(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.read(id));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @GetMapping("/email/{email}")
    public ResponseEntity<User> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<User> findByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(userService.findByPhone(phone));
    }

    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.existsByEmail(email));
    }

    @GetMapping("/exists/phone")
    public ResponseEntity<Boolean> existsByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(userService.existsByPhone(phone));
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> findByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }

    @GetMapping("/active")
    public ResponseEntity<List<User>> findByActive(@RequestParam(defaultValue = "true") boolean active) {
        return ResponseEntity.ok(userService.findByActive(active));
    }

    @GetMapping("/role/{role}/active")
    public ResponseEntity<List<User>> findByRoleAndActive(
            @PathVariable Role role, @RequestParam(defaultValue = "true") boolean active) {
        return ResponseEntity.ok(userService.findByRoleAndActive(role, active));
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<User> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<User> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activate(id));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<User> changePassword(
            @PathVariable UUID id, @RequestParam String newPassword) {
        return ResponseEntity.ok(userService.changePassword(id, newPassword));
    }
}
