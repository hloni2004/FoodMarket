package com.llburgers.repository;

import com.llburgers.domain.Admin;
import com.llburgers.domain.enums.AdminLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Admin> findByAdminLevel(AdminLevel adminLevel);

    List<Admin> findByActive(boolean active);

    List<Admin> findByAdminLevelAndActive(AdminLevel adminLevel, boolean active);
}

