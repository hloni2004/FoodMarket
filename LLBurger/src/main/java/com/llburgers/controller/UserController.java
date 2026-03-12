package com.llburgers.controller;

import com.llburgers.domain.User;
import com.llburgers.domain.enums.Role;
import com.llburgers.security.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;

    public UserController(IUserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
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
        userService.deactivate(id);
        refreshTokenService.revokeAllForUser(id);
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
        User user = userService.deactivate(id);
        // SECURITY: Revoke all refresh tokens when account is deactivated
        refreshTokenService.revokeAllForUser(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<User> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activate(id));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<User> changePassword(
            @PathVariable UUID id, @RequestParam String newPassword) {
        User user = userService.changePassword(id, newPassword);
        // SECURITY: Revoke all refresh tokens when password is changed
        refreshTokenService.revokeAllForUser(id);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<User> changeRole(
            @PathVariable UUID id, @RequestParam Role role) {
        User user = userService.read(id);
        user.setRole(role);
        User updated = userService.update(user);
        // SECURITY: Revoke all refresh tokens when role is changed
        refreshTokenService.revokeAllForUser(id);
        return ResponseEntity.ok(updated);
    }
}
