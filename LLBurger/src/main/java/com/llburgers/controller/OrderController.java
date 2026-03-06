package com.llburgers.controller;

import com.llburgers.domain.Order;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.OrderStatus;
import com.llburgers.service.IOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> read(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.read(id));
    }

    @PutMapping
    public ResponseEntity<Order> update(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.update(order));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── By Customer ──────────────────────────────────────────────────────────

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> findByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(orderService.findByCustomerId(customerId));
    }

    @GetMapping("/customer/{customerId}/status/{status}")
    public ResponseEntity<List<Order>> findByCustomerIdAndStatus(
            @PathVariable UUID customerId, @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.findByCustomerIdAndStatus(customerId, status));
    }

    @GetMapping("/customer/{customerId}/history")
    public ResponseEntity<List<Order>> findOrderHistory(@PathVariable UUID customerId) {
        return ResponseEntity.ok(orderService.findOrderHistory(customerId));
    }

    // ─── By Status ────────────────────────────────────────────────────────────

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> findByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.findByStatus(status));
    }

    @GetMapping("/status/{status}/count")
    public ResponseEntity<Long> countByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.countByStatus(status));
    }

    // ─── By Delivery Location ─────────────────────────────────────────────────

    @GetMapping("/delivery/block/{block}")
    public ResponseEntity<List<Order>> findByDeliveryBlock(@PathVariable Block block) {
        return ResponseEntity.ok(orderService.findByDeliveryBlock(block));
    }

    @GetMapping("/delivery/block/{block}/status/{status}")
    public ResponseEntity<List<Order>> findByDeliveryBlockAndStatus(
            @PathVariable Block block, @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.findByDeliveryBlockAndStatus(block, status));
    }

    // ─── By Date ──────────────────────────────────────────────────────────────

    @GetMapping("/date-range")
    public ResponseEntity<List<Order>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(orderService.findByDateRange(from, to));
    }

    @GetMapping("/status/{status}/date-range")
    public ResponseEntity<List<Order>> findByStatusAndDateRange(
            @PathVariable OrderStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(orderService.findByStatusAndDateRange(status, from, to));
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable UUID id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancel(id));
    }
}
