package com.llburgers.service;

import com.llburgers.domain.Rating;

import java.util.List;
import java.util.UUID;

public interface IRatingService extends IService<Rating, UUID> {

    // ─── By Order ─────────────────────────────────────────────────────────────

    Rating findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    // ─── By Score ─────────────────────────────────────────────────────────────

    List<Rating> findByFoodRating(int foodRating);

    List<Rating> findByDeliveryRating(int deliveryRating);

    List<Rating> findByFoodRatingGreaterThanEqual(int minRating);

    List<Rating> findByDeliveryRatingGreaterThanEqual(int minRating);

    // ─── Aggregates ───────────────────────────────────────────────────────────

    Double getAverageFoodRating();

    Double getAverageDeliveryRating();
}

