package com.llburgers.factory;

import com.llburgers.domain.Side;
import com.llburgers.util.Helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SideFactory {

    /**
     * Creates a validated Side object.
     *
     * @param name            side name
     * @param price           side price (must be >= 0)
     * @param stockQuantity   stock quantity (must be >= 0)
     * @param imageUrl        image URL (optional)
     * @param availability    availability flag (default: true)
     * @return a new Side instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Side createSide(String name, BigDecimal price, int stockQuantity,
                                 String imageUrl, boolean availability) {
        List<String> errors = new ArrayList<>();

        // Validate side name
        if (!Helper.isValidLength(name, 1, 255)) {
            errors.add("Side name is invalid (must be 1–255 characters)");
        }

        // Validate price
        if (price == null || !Helper.isValidPrice(price.doubleValue())) {
            errors.add("Price must be a non-negative number");
        }

        // Validate stock quantity
        if (!Helper.isValidQuantity(stockQuantity)) {
            errors.add("Stock quantity must be non-negative");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Side creation failed — " + String.join("; ", errors));
        }

        return Side.builder()
                .name(name.trim())
                .price(price)
                .stockQuantity(stockQuantity)
                .imageUrl(imageUrl != null && !Helper.isNullOrEmpty(imageUrl) ? imageUrl.trim() : null)
                .availability(availability)
                .build();
    }

    /**
     * Creates a Side with default availability (true) and no image URL.
     *
     * @param name          side name
     * @param price         side price
     * @param stockQuantity stock quantity
     * @return a new Side instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Side createSide(String name, BigDecimal price, int stockQuantity) {
        return createSide(name, price, stockQuantity, null, true);
    }
}

