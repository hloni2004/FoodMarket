package com.llburgers.service.impl;

import com.llburgers.domain.User;
import com.llburgers.domain.enums.Role;
import com.llburgers.repository.UserRepository;
import com.llburgers.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for base {@link User} operations.
 * <p>
 * Handles cross-cutting concerns shared by both {@code Admin} and {@code Customer}:
 * lookups, activation toggling, and password changes.
 */
@Service
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public User read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Override
    public User update(User user) {
        if (user.getId() == null || !repository.existsById(user.getId())) {
            throw new IllegalArgumentException("User not found with id: " + user.getId());
        }
        return repository.save(user);
    }

    @Override
    public List<User> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        repository.deleteById(id);
        log.info("[USER-DELETED] id={}", id);
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @Override
    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Override
    public User findByPhone(String phone) {
        return repository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found with phone: " + phone));
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return repository.existsByPhone(phone);
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @Override
    public List<User> findByRole(Role role) {
        return repository.findByRole(role);
    }

    @Override
    public List<User> findByActive(boolean active) {
        return repository.findByActive(active);
    }

    @Override
    public List<User> findByRoleAndActive(Role role, boolean active) {
        return repository.findByRoleAndActive(role, active);
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @Override
    public User deactivate(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setActive(false);
        User saved = repository.save(user);
        log.info("[USER-DEACTIVATED] id={}, role={}", saved.getId(), saved.getRole());
        return saved;
    }

    @Override
    public User activate(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setActive(true);
        User saved = repository.save(user);
        log.info("[USER-ACTIVATED] id={}, role={}", saved.getId(), saved.getRole());
        return saved;
    }

    @Override
    public User changePassword(UUID id, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setPassword(newPassword);
        User saved = repository.save(user);
        log.info("[USER-PASSWORD-CHANGED] id={}", saved.getId());
        return saved;
    }
}

