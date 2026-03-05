package com.llburgers.repository;

import com.llburgers.domain.Customer;
import com.llburgers.domain.enums.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Customer> findByBlock(Block block);

    List<Customer> findByBlockAndRoomNumber(Block block, String roomNumber);

    List<Customer> findByActive(boolean active);
}

