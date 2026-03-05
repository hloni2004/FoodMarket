package com.llburgers.service.impl;

import com.llburgers.domain.Customer;
import com.llburgers.domain.enums.Block;
import com.llburgers.repository.CustomerRepository;
import com.llburgers.service.ICustomerService;
import com.llburgers.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements ICustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository repository;
    private final IEmailService emailService;

    public CustomerServiceImpl(CustomerRepository repository, IEmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    // ─── CRUD (from IService) ─────────────────────────────────────────────────

    @Override
    public Customer create(Customer customer) {
        Customer saved = repository.save(customer);

        // Send welcome email via Brevo (Provider A)
        try {
            emailService.sendWelcomeEmail(saved);
        } catch (Exception e) {
            log.error("[CUSTOMER] Failed to send welcome email to {}: {}",
                    saved.getEmail(), e.getMessage());
        }

        log.info("[CUSTOMER-REGISTERED] id={}, email={}, block={}, room={}",
                saved.getId(), saved.getEmail(), saved.getBlock(), saved.getRoomNumber());
        return saved;
    }

    @Override
    public Customer read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
    }

    @Override
    public Customer update(Customer customer) {
        if (customer.getId() == null || !repository.existsById(customer.getId())) {
            throw new IllegalArgumentException("Customer not found with id: " + customer.getId());
        }
        return repository.save(customer);
    }

    @Override
    public List<Customer> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Customer not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @Override
    public Customer findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with email: " + email));
    }

    @Override
    public Customer findByPhone(String phone) {
        return repository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with phone: " + phone));
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
    public List<Customer> findByBlock(Block block) {
        return repository.findByBlock(block);
    }

    @Override
    public List<Customer> findByBlockAndRoomNumber(Block block, String roomNumber) {
        return repository.findByBlockAndRoomNumber(block, roomNumber);
    }

    @Override
    public List<Customer> findByActive(boolean active) {
        return repository.findByActive(active);
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @Override
    public Customer deactivate(UUID id) {
        Customer customer = read(id);
        customer.setActive(false);
        return repository.save(customer);
    }

    @Override
    public Customer activate(UUID id) {
        Customer customer = read(id);
        customer.setActive(true);
        return repository.save(customer);
    }

    @Override
    public Customer updateDeliveryDetails(UUID id, Block block, String roomNumber) {
        Customer customer = read(id);
        customer.setBlock(block);
        customer.setRoomNumber(roomNumber);
        return repository.save(customer);
    }
}

