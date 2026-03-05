package com.llburgers.service.impl;

import com.llburgers.domain.Rating;
import com.llburgers.repository.RatingRepository;
import com.llburgers.service.IRatingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RatingServiceImpl implements IRatingService {

    private final RatingRepository repository;

    public RatingServiceImpl(RatingRepository repository) {
        this.repository = repository;
    }

    // ─── CRUD (from IService) ─────────────────────────────────────────────────

    @Override
    public Rating create(Rating rating) {
        if (repository.existsByOrderId(rating.getOrder().getId())) {
            throw new IllegalStateException("A rating already exists for order: " + rating.getOrder().getId());
        }
        return repository.save(rating);
    }

    @Override
    public Rating read(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found with id: " + id));
    }

    @Override
    public Rating update(Rating rating) {
        if (rating.getId() == null || !repository.existsById(rating.getId())) {
            throw new IllegalArgumentException("Rating not found with id: " + rating.getId());
        }
        return repository.save(rating);
    }

    @Override
    public List<Rating> getAll() {
        return repository.findAll();
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Rating not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // ─── By Order ─────────────────────────────────────────────────────────────

    @Override
    public Rating findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found for order: " + orderId));
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return repository.existsByOrderId(orderId);
    }

    // ─── By Score ─────────────────────────────────────────────────────────────

    @Override
    public List<Rating> findByFoodRating(int foodRating) {
        return repository.findByFoodRating(foodRating);
    }

    @Override
    public List<Rating> findByDeliveryRating(int deliveryRating) {
        return repository.findByDeliveryRating(deliveryRating);
    }

    @Override
    public List<Rating> findByFoodRatingGreaterThanEqual(int minRating) {
        return repository.findByFoodRatingGreaterThanEqual(minRating);
    }

    @Override
    public List<Rating> findByDeliveryRatingGreaterThanEqual(int minRating) {
        return repository.findByDeliveryRatingGreaterThanEqual(minRating);
    }

    // ─── Aggregates ───────────────────────────────────────────────────────────

    @Override
    public Double getAverageFoodRating() {
        return repository.findAverageFoodRating();
    }

    @Override
    public Double getAverageDeliveryRating() {
        return repository.findAverageDeliveryRating();
    }
}

