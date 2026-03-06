package com.llburgers.controller;

import com.llburgers.domain.Rating;
import com.llburgers.service.IRatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin
public class RatingController {

    private final IRatingService ratingService;

    public RatingController(IRatingService ratingService) {
        this.ratingService = ratingService;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Rating> create(@RequestBody Rating rating) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.create(rating));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rating> read(@PathVariable UUID id) {
        return ResponseEntity.ok(ratingService.read(id));
    }

    @PutMapping
    public ResponseEntity<Rating> update(@RequestBody Rating rating) {
        return ResponseEntity.ok(ratingService.update(rating));
    }

    @GetMapping
    public ResponseEntity<List<Rating>> getAll() {
        return ResponseEntity.ok(ratingService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ratingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─── By Order ─────────────────────────────────────────────────────────────

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Rating> findByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ratingService.findByOrderId(orderId));
    }

    @GetMapping("/order/{orderId}/exists")
    public ResponseEntity<Boolean> existsByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ratingService.existsByOrderId(orderId));
    }

    // ─── By Score ─────────────────────────────────────────────────────────────

    @GetMapping("/food-rating/{rating}")
    public ResponseEntity<List<Rating>> findByFoodRating(@PathVariable int rating) {
        return ResponseEntity.ok(ratingService.findByFoodRating(rating));
    }

    @GetMapping("/delivery-rating/{rating}")
    public ResponseEntity<List<Rating>> findByDeliveryRating(@PathVariable int rating) {
        return ResponseEntity.ok(ratingService.findByDeliveryRating(rating));
    }

    @GetMapping("/food-rating/min/{minRating}")
    public ResponseEntity<List<Rating>> findByFoodRatingGreaterThanEqual(@PathVariable int minRating) {
        return ResponseEntity.ok(ratingService.findByFoodRatingGreaterThanEqual(minRating));
    }

    @GetMapping("/delivery-rating/min/{minRating}")
    public ResponseEntity<List<Rating>> findByDeliveryRatingGreaterThanEqual(@PathVariable int minRating) {
        return ResponseEntity.ok(ratingService.findByDeliveryRatingGreaterThanEqual(minRating));
    }

    // ─── Aggregates ───────────────────────────────────────────────────────────

    @GetMapping("/average/food")
    public ResponseEntity<Double> getAverageFoodRating() {
        return ResponseEntity.ok(ratingService.getAverageFoodRating());
    }

    @GetMapping("/average/delivery")
    public ResponseEntity<Double> getAverageDeliveryRating() {
        return ResponseEntity.ok(ratingService.getAverageDeliveryRating());
    }
}
