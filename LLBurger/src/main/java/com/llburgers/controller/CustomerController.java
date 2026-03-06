package com.llburgers.controller;

import com.llburgers.domain.Customer;
import com.llburgers.domain.enums.Block;
import com.llburgers.service.ICustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin
public class CustomerController {

    private final ICustomerService customerService;

    public CustomerController(ICustomerService customerService) {
        this.customerService = customerService;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(customer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> read(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.read(id));
    }

    @PutMapping
    public ResponseEntity<Customer> update(@RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.update(customer));
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(customerService.findByEmail(email));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Customer> findByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(customerService.findByPhone(phone));
    }

    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(customerService.existsByEmail(email));
    }

    @GetMapping("/exists/phone")
    public ResponseEntity<Boolean> existsByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(customerService.existsByPhone(phone));
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @GetMapping("/block/{block}")
    public ResponseEntity<List<Customer>> findByBlock(@PathVariable Block block) {
        return ResponseEntity.ok(customerService.findByBlock(block));
    }

    @GetMapping("/block/{block}/room/{roomNumber}")
    public ResponseEntity<List<Customer>> findByBlockAndRoomNumber(
            @PathVariable Block block, @PathVariable String roomNumber) {
        return ResponseEntity.ok(customerService.findByBlockAndRoomNumber(block, roomNumber));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Customer>> findByActive(@RequestParam(defaultValue = "true") boolean active) {
        return ResponseEntity.ok(customerService.findByActive(active));
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Customer> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Customer> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.activate(id));
    }

    @PatchMapping("/{id}/delivery-details")
    public ResponseEntity<Customer> updateDeliveryDetails(
            @PathVariable UUID id, @RequestParam Block block, @RequestParam String roomNumber) {
        return ResponseEntity.ok(customerService.updateDeliveryDetails(id, block, roomNumber));
    }
}
