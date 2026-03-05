package com.llburgers.service;

import com.llburgers.domain.User;
import com.llburgers.domain.enums.Role;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for base {@link User} operations.
 * <p>
 * Covers shared behaviour that applies to both {@code Admin} and {@code Customer}:
 * lookup by email / phone, activate / deactivate, password change, and role filtering.
 * <p>
 * Subtype-specific operations (delivery details, admin level, etc.) are handled
 * by {@link ICustomerService} and {@link IAdminService} respectively.
 */
public interface IUserService extends IService<User, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    User findByEmail(String email);

    User findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<User> findByRole(Role role);

    List<User> findByActive(boolean active);

    List<User> findByRoleAndActive(Role role, boolean active);

    // ─── Business Logic ───────────────────────────────────────────────────────

    /**
     * Deactivates a user account (sets active = false).
     * Works for both Admin and Customer subtypes.
     */
    User deactivate(UUID id);

    /**
     * Reactivates a user account (sets active = true).
     */
    User activate(UUID id);

    /**
     * Updates the user's password.
     *
     * @param id          the user's UUID
     * @param newPassword the new plain-text password (hashing handled by caller / security layer)
     */
    User changePassword(UUID id, String newPassword);
}

