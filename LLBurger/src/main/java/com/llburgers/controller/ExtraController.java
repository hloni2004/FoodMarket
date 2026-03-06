package com.llburgers.controller;

import com.llburgers.domain.Extra;
import com.llburgers.service.IExtraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/extras")
@CrossOrigin
public class ExtraController {

    private final IExtraService extraService;

    public ExtraController(IExtraService extraService) {
        this.extraService = extraService;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Extra> create(@RequestBody Extra extra) {
        return ResponseEntity.status(HttpStatus.CREATED).body(extraService.create(extra));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Extra> read(@PathVariable UUID id) {
        return ResponseEntity.ok(extraService.read(id));
    }

    @PutMapping
    public ResponseEntity<Extra> update(@RequestBody Extra extra) {
        return ResponseEntity.ok(extraService.update(extra));
    }

    @GetMapping
    public ResponseEntity<List<Extra>> getAll() {
        return ResponseEntity.ok(extraService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        extraService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByName(@RequestParam String name) {
        return ResponseEntity.ok(extraService.existsByName(name));
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @GetMapping("/available")
    public ResponseEntity<List<Extra>> findByAvailability(@RequestParam(defaultValue = "true") boolean availability) {
        return ResponseEntity.ok(extraService.findByAvailability(availability));
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<Extra>> findByPriceBetween(
            @RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return ResponseEntity.ok(extraService.findByPriceBetween(min, max));
    }

    // ─── Stock Management ─────────────────────────────────────────────────────

    @GetMapping("/in-stock")
    public ResponseEntity<List<Extra>> findInStock() {
        return ResponseEntity.ok(extraService.findInStock());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Extra>> findOutOfStock() {
        return ResponseEntity.ok(extraService.findOutOfStock());
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Extra> updateStock(@PathVariable UUID id, @RequestParam int quantity) {
        return ResponseEntity.ok(extraService.updateStock(id, quantity));
    }

    @PatchMapping("/{id}/reduce-stock")
    public ResponseEntity<Extra> reduceStock(@PathVariable UUID id, @RequestParam int quantity) {
        return ResponseEntity.ok(extraService.reduceStock(id, quantity));
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/unavailable")
    public ResponseEntity<Extra> markUnavailable(@PathVariable UUID id) {
        return ResponseEntity.ok(extraService.markUnavailable(id));
    }

    @PatchMapping("/{id}/available")
    public ResponseEntity<Extra> markAvailable(@PathVariable UUID id) {
        return ResponseEntity.ok(extraService.markAvailable(id));
    }

    // ─── Image Management ─────────────────────────────────────────────────────

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Extra> uploadImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(extraService.uploadImage(id, file));
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Extra> removeImage(@PathVariable UUID id) {
        return ResponseEntity.ok(extraService.removeImage(id));
    }
}
