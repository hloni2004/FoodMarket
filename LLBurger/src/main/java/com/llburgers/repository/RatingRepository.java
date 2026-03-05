package com.llburgers.repository;

import com.llburgers.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    // ─── By Order ─────────────────────────────────────────────────────────────

    Optional<Rating> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    // ─── By Rating Score ──────────────────────────────────────────────────────

    List<Rating> findByFoodRating(int foodRating);

    List<Rating> findByDeliveryRating(int deliveryRating);

    List<Rating> findByFoodRatingGreaterThanEqual(int minRating);

    List<Rating> findByDeliveryRatingGreaterThanEqual(int minRating);

    // ─── Aggregates ───────────────────────────────────────────────────────────

    @Query("SELECT AVG(r.foodRating) FROM Rating r")
    Double findAverageFoodRating();

    @Query("SELECT AVG(r.deliveryRating) FROM Rating r")
    Double findAverageDeliveryRating();
}

