package com.llburgers.service;

import com.llburgers.domain.Admin;
import com.llburgers.domain.enums.AdminLevel;

import java.util.List;
import java.util.UUID;

public interface IAdminService extends IService<Admin, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    Admin findByEmail(String email);

    boolean existsByEmail(String email);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Admin> findByAdminLevel(AdminLevel adminLevel);

    List<Admin> findByActive(boolean active);

    List<Admin> findByAdminLevelAndActive(AdminLevel adminLevel, boolean active);

    // ─── Business Logic ───────────────────────────────────────────────────────

    Admin deactivate(UUID id);

    Admin activate(UUID id);

    Admin updateAdminLevel(UUID id, AdminLevel adminLevel);
}

