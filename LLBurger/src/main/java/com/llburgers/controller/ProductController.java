package com.llburgers.controller;

import com.llburgers.domain.Product;
import com.llburgers.domain.enums.ProductCategory;
import com.llburgers.service.IProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> read(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.read(id));
    }

    @PutMapping
    public ResponseEntity<Product> update(@RequestBody Product product) {
        return ResponseEntity.ok(productService.update(product));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Lookup ───────────────────────────────────────────────────────────────

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.existsByName(name));
    }

    // ─── Filtering ────────────────────────────────────────────────────────────

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> findByCategory(@PathVariable ProductCategory category) {
        return ResponseEntity.ok(productService.findByCategory(category));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Product>> findByAvailability(@RequestParam(defaultValue = "true") boolean availability) {
        return ResponseEntity.ok(productService.findByAvailability(availability));
    }

    @GetMapping("/category/{category}/available")
    public ResponseEntity<List<Product>> findByCategoryAndAvailability(
            @PathVariable ProductCategory category,
            @RequestParam(defaultValue = "true") boolean availability) {
        return ResponseEntity.ok(productService.findByCategoryAndAvailability(category, availability));
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> findByPriceBetween(
            @RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return ResponseEntity.ok(productService.findByPriceBetween(min, max));
    }

    // ─── Stock Management ─────────────────────────────────────────────────────

    @GetMapping("/in-stock")
    public ResponseEntity<List<Product>> findInStock() {
        return ResponseEntity.ok(productService.findInStock());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<Product>> findOutOfStock() {
        return ResponseEntity.ok(productService.findOutOfStock());
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable UUID id, @RequestParam int quantity) {
        return ResponseEntity.ok(productService.updateStock(id, quantity));
    }

    @PatchMapping("/{id}/reduce-stock")
    public ResponseEntity<Product> reduceStock(@PathVariable UUID id, @RequestParam int quantity) {
        return ResponseEntity.ok(productService.reduceStock(id, quantity));
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @PatchMapping("/{id}/unavailable")
    public ResponseEntity<Product> markUnavailable(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.markUnavailable(id));
    }

    @PatchMapping("/{id}/available")
    public ResponseEntity<Product> markAvailable(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.markAvailable(id));
    }

    // ─── Image Management ─────────────────────────────────────────────────────

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> uploadImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(productService.uploadImage(id, file));
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Product> removeImage(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.removeImage(id));
    }
}
