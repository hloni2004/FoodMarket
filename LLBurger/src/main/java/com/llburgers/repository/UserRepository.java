package com.llburgers.repository;

import com.llburgers.domain.User;
import com.llburgers.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<User> findByRole(Role role);

    List<User> findByActive(boolean active);

    List<User> findByRoleAndActive(Role role, boolean active);
}

