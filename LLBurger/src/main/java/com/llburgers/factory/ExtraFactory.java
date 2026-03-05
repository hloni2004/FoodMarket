package com.llburgers.factory;

import com.llburgers.domain.Extra;
import com.llburgers.util.Helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExtraFactory {

    /**
     * Creates a validated Extra object.
     *
     * @param name            extra name
     * @param price           extra price (must be >= 0)
     * @param stockQuantity   stock quantity (must be >= 0)
     * @param imageUrl        image URL (optional, stored in Supabase Storage)
     * @param availability    availability flag (default: true)
     * @return a new Extra instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Extra createExtra(String name, BigDecimal price, int stockQuantity,
                                    String imageUrl, boolean availability) {
        List<String> errors = new ArrayList<>();

        // Validate extra name
        if (!Helper.isValidLength(name, 1, 255)) {
            errors.add("Extra name is invalid (must be 1–255 characters)");
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
            throw new IllegalArgumentException("Extra creation failed — " + String.join("; ", errors));
        }

        return Extra.builder()
                .name(name.trim())
                .price(price)
                .stockQuantity(stockQuantity)
                .imageUrl(imageUrl != null && !Helper.isNullOrEmpty(imageUrl) ? imageUrl.trim() : null)
                .availability(availability)
                .build();
    }

    /**
     * Creates an Extra with default availability (true) and a given imageUrl.
     *
     * @param name          extra name
     * @param price         extra price
     * @param stockQuantity stock quantity
     * @param imageUrl      image URL (optional, stored in Supabase Storage)
     * @return a new Extra instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Extra createExtra(String name, BigDecimal price, int stockQuantity, String imageUrl) {
        return createExtra(name, price, stockQuantity, imageUrl, true);
    }

    /**
     * Creates an Extra with default availability (true) and no image URL.
     *
     * @param name          extra name
     * @param price         extra price
     * @param stockQuantity stock quantity
     * @return a new Extra instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Extra createExtra(String name, BigDecimal price, int stockQuantity) {
        return createExtra(name, price, stockQuantity, null, true);
    }

    /**
     * Creates an Extra with a provided availability flag and no image URL.
     * Kept for backward compatibility with existing callers that pass (name, price, stock, availability).
     *
     * @param name            extra name
     * @param price           extra price (must be >= 0)
     * @param stockQuantity   stock quantity (must be >= 0)
     * @param availability    availability flag
     * @return a new Extra instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Extra createExtra(String name, BigDecimal price, int stockQuantity, boolean availability) {
        return createExtra(name, price, stockQuantity, null, availability);
    }
}
