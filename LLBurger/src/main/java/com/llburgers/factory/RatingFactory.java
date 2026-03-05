package com.llburgers.factory;

import com.llburgers.domain.Order;
import com.llburgers.domain.Rating;
import com.llburgers.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class RatingFactory {

    /**
     * Creates a validated Rating object.
     *
     * @param order            the associated order
     * @param foodRating       food rating (1–5 stars)
     * @param deliveryRating   delivery rating (1–5 stars)
     * @param feedback         customer feedback (optional)
     * @return a new Rating instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Rating createRating(Order order, int foodRating, int deliveryRating, String feedback) {
        List<String> errors = new ArrayList<>();

        // Validate order
        if (order == null) {
            errors.add("Order cannot be null");
        }

        // Validate food rating
        if (!Helper.isValidRating(foodRating)) {
            errors.add("Food rating must be between 1 and 5 stars");
        }

        // Validate delivery rating
        if (!Helper.isValidRating(deliveryRating)) {
            errors.add("Delivery rating must be between 1 and 5 stars");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Rating creation failed — " + String.join("; ", errors));
        }

        return Rating.builder()
                .order(order)
                .foodRating(foodRating)
                .deliveryRating(deliveryRating)
                .feedback(feedback != null && !Helper.isNullOrEmpty(feedback) ? feedback.trim() : null)
                .build();
    }

    /**
     * Creates a Rating without feedback.
     *
     * @param order          the associated order
     * @param foodRating     food rating
     * @param deliveryRating delivery rating
     * @return a new Rating instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Rating createRating(Order order, int foodRating, int deliveryRating) {
        return createRating(order, foodRating, deliveryRating, null);
    }
}

