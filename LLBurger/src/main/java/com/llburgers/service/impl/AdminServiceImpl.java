package com.llburgers.service.impl;

import com.llburgers.domain.Admin;
import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.repository.AdminRepository;
import com.llburgers.service.IAdminService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminServiceImpl implements IAdminService {

    private final AdminRepository repository;

    public AdminServiceImpl(AdminRepository repository) {
        this.repository = repository;
    }

    // ─── CRUD (from IService) ─────────────────────────────────────────────────

    @Override
    public Admin create(Admin admin) {
        return repository.save(admin);
    }

    @Override
    public Admin read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));
    }

    @Override
    public Admin update(Admin admin) {
        if (admin.getId() == null || !repository.existsById(admin.getId())) {
            throw new IllegalArgumentException("Admin not found with id: " + admin.getId());
        }
        return repository.save(admin);
    }

    @Override
    public List<Admin> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Admin not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @Override
    public Admin findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with email: " + email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @Override
    public List<Admin> findByAdminLevel(AdminLevel adminLevel) {
        return repository.findByAdminLevel(adminLevel);
    }

    @Override
    public List<Admin> findByActive(boolean active) {
        return repository.findByActive(active);
    }

    @Override
    public List<Admin> findByAdminLevelAndActive(AdminLevel adminLevel, boolean active) {
        return repository.findByAdminLevelAndActive(adminLevel, active);
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @Override
    public Admin deactivate(UUID id) {
        Admin admin = read(id);
        admin.setActive(false);
        return repository.save(admin);
    }

    @Override
    public Admin activate(UUID id) {
        Admin admin = read(id);
        admin.setActive(true);
        return repository.save(admin);
    }

    @Override
    public Admin updateAdminLevel(UUID id, AdminLevel adminLevel) {
        Admin admin = read(id);
        admin.setAdminLevel(adminLevel);
        return repository.save(admin);
    }
}

