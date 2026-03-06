package com.llburgers.controller;

import com.llburgers.domain.Side;
import com.llburgers.service.ISideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sides")
@CrossOrigin
public class SideController {

    private final ISideService sideService;

    public SideController(ISideService sideService) {
        this.sideService = sideService;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Side> create(@RequestBody Side side) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sideService.create(side));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Side> read(@PathVariable UUID id) {
        return ResponseEntity.ok(sideService.read(id));
    }

    @PutMapping
    public ResponseEntity<Side> update(@RequestBody Side side) {
        return ResponseEntity.ok(sideService.update(side));
    }

    @GetMapping
    public ResponseEntity<List<Side>> getAll() {
        return ResponseEntity.ok(sideService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sideService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByName(@RequestParam String name) {
        return ResponseEntity.ok(sideService.existsByName(name));
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @GetMapping("/available")
    public ResponseEntity<List<Side>> findByAvailability(@RequestParam(defaultValue = "true") boolean availability) {
        return ResponseEntity.ok(sideService.findByAvailability(availability));
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<Side>> findByPriceBetween(
            @RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return ResponseEntity.ok(sideService.findByPriceBetween(min, max));
    }

    // ─── Stock Management ─────────────────────────────────────────────────────

    @GetMapping("/in-stock")
    public ResponseEntity<List<Side>> findInStock() {
        return ResponseEntity.ok(sideService.findInStock());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Side>> findOutOfStock() {
        return ResponseEntity.ok(sideService.findOutOfStock());
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Side> updateStock(@PathVariable UUID id, @RequestParam int quantity) {
        return ResponseEntity.ok(sideService.updateStock(id, quantity));
    }

    @PatchMapping("/{id}/reduce-stock")
    public ResponseEntity<Side> reduceStock(@PathVariable UUID id, @RequestParam int quantity) {
        return ResponseEntity.ok(sideService.reduceStock(id, quantity));
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/unavailable")
    public ResponseEntity<Side> markUnavailable(@PathVariable UUID id) {
        return ResponseEntity.ok(sideService.markUnavailable(id));
    }

    @PatchMapping("/{id}/available")
    public ResponseEntity<Side> markAvailable(@PathVariable UUID id) {
        return ResponseEntity.ok(sideService.markAvailable(id));
    }

    // ─── Image Management ─────────────────────────────────────────────────────

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Side> uploadImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(sideService.uploadImage(id, file));
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Side> removeImage(@PathVariable UUID id) {
        return ResponseEntity.ok(sideService.removeImage(id));
    }
}
