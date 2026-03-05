package com.llburgers.factory;

import com.llburgers.domain.Product;
import com.llburgers.domain.enums.ProductCategory;
import com.llburgers.util.Helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductFactory {

    /**
     * Creates a validated Product object.
     *
     * @param name            product name
     * @param price           product price (must be >= 0)
     * @param category        product category
     * @param stockQuantity   stock quantity (must be >= 0)
     * @param imageUrl        image URL (optional)
     * @param availability    availability flag (default: true)
     * @return a new Product instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Product createProduct(String name, BigDecimal price, ProductCategory category,
                                       int stockQuantity, String imageUrl, boolean availability) {
        List<String> errors = new ArrayList<>();

        // Validate product name
        if (!Helper.isValidLength(name, 1, 255)) {
            errors.add("Product name is invalid (must be 1–255 characters)");
        }

        // Validate price
        if (price == null || !Helper.isValidPrice(price.doubleValue())) {
            errors.add("Price must be a non-negative number");
        }

        // Validate category
        if (category == null) {
            errors.add("Category cannot be null");
        }

        // Validate stock quantity
        if (!Helper.isValidQuantity(stockQuantity)) {
            errors.add("Stock quantity must be non-negative");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Product creation failed — " + String.join("; ", errors));
        }

        return Product.builder()
                .name(name.trim())
                .price(price)
                .category(category)
                .stockQuantity(stockQuantity)
                .imageUrl(imageUrl != null && !Helper.isNullOrEmpty(imageUrl) ? imageUrl.trim() : null)
                .availability(availability)
                .build();
    }

    /**
     * Creates a Product with default availability (true) and no image URL.
     *
     * @param name          product name
     * @param price         product price
     * @param category      product category
     * @param stockQuantity stock quantity
     * @return a new Product instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Product createProduct(String name, BigDecimal price, ProductCategory category,
                                       int stockQuantity) {
        return createProduct(name, price, category, stockQuantity, null, true);
    }
}

