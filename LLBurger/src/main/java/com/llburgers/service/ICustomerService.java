package com.llburgers.service;

import com.llburgers.domain.Customer;
import com.llburgers.domain.enums.Block;

import java.util.List;
import java.util.UUID;

public interface ICustomerService extends IService<Customer, UUID> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    Customer findByEmail(String email);

    Customer findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // ─── Filtering ────────────────────────────────────────────────────────────

    List<Customer> findByBlock(Block block);

    List<Customer> findByBlockAndRoomNumber(Block block, String roomNumber);

    List<Customer> findByActive(boolean active);

    // ─── Business Logic ───────────────────────────────────────────────────────

    Customer deactivate(UUID id);

    Customer activate(UUID id);

    Customer updateDeliveryDetails(UUID id, Block block, String roomNumber);
}

