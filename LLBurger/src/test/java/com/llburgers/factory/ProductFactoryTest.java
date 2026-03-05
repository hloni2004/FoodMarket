package com.llburgers.factory;

import com.llburgers.domain.Product;
import com.llburgers.domain.enums.ProductCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductFactoryTest {

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createProduct_validInputWithImage_returnsProductWithCorrectAttributes() {
        Product product = ProductFactory.createProduct(
                "Delicious Burger",
                new BigDecimal("45.99"),
                ProductCategory.BURGER,
                50,
                "https://vlklxrrgvaoykpkvdoxe.supabase.co/storage/v1/object/public/llburger-images/products/delicious-burger.jpg",
                true
        );

        assertNotNull(product);
        assertEquals("Delicious Burger", product.getName());
        assertEquals(new BigDecimal("45.99"), product.getPrice());
        assertEquals(ProductCategory.BURGER, product.getCategory());
        assertEquals(50, product.getStockQuantity());
        assertNotNull(product.getImageUrl());
        assertTrue(product.getImageUrl().contains("products/delicious-burger.jpg"));
        assertTrue(product.isAvailability());
        System.out.println("\n✓ Test 1 - Valid Product Creation (with image):");
        System.out.println("  Name       : " + product.getName());
        System.out.println("  Price      : R" + product.getPrice());
        System.out.println("  Category   : " + product.getCategory());
        System.out.println("  Stock      : " + product.getStockQuantity());
        System.out.println("  Image URL  : " + product.getImageUrl());
        System.out.println("  Available  : " + product.isAvailability());
    }

    @Test
    void createProduct_withDefaultAvailability_returnsProductWithTrueAvailabilityAndNullImage() {
        Product product = ProductFactory.createProduct(
                "Crispy Fries",
                new BigDecimal("12.50"),
                ProductCategory.SAUCE,
                100
        );

        assertNotNull(product);
        assertEquals("Crispy Fries", product.getName());
        assertEquals(ProductCategory.SAUCE, product.getCategory());
        assertTrue(product.isAvailability());
        assertNull(product.getImageUrl());
        System.out.println("\n✓ Test 2 - Product with Default Availability (no image):");
        System.out.println("  Name       : " + product.getName());
        System.out.println("  Price      : R" + product.getPrice());
        System.out.println("  Category   : " + product.getCategory());
        System.out.println("  Stock      : " + product.getStockQuantity());
        System.out.println("  Image URL  : " + product.getImageUrl());
        System.out.println("  Available  : " + product.isAvailability());
    }

    @Test
    void createProduct_zeroPrice_isValid() {
        Product product = ProductFactory.createProduct(
                "Free Sample",
                BigDecimal.ZERO,
                ProductCategory.BURGER,
                10,
                "https://vlklxrrgvaoykpkvdoxe.supabase.co/storage/v1/object/public/llburger-images/products/free-sample.jpg",
                true
        );

        assertNotNull(product);
        assertEquals(BigDecimal.ZERO, product.getPrice());
        assertNotNull(product.getImageUrl());
        System.out.println("\n✓ Test 3 - Product with Zero Price:");
        System.out.println("  Name       : " + product.getName());
        System.out.println("  Price      : R" + product.getPrice());
        System.out.println("  Category   : " + product.getCategory());
        System.out.println("  Stock      : " + product.getStockQuantity());
        System.out.println("  Image URL  : " + product.getImageUrl());
        System.out.println("  Available  : " + product.isAvailability());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createProduct_nullName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct(
                        null,
                        new BigDecimal("45.99"),
                        ProductCategory.BURGER,
                        50
                )
        );
        assertTrue(ex.getMessage().contains("Product creation failed"));
        System.out.println("\n✗ Test 4 - Null Name Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createProduct_nullPrice_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct(
                        "Burger",
                        null,
                        ProductCategory.BURGER,
                        50
                )
        );
        assertTrue(ex.getMessage().contains("Product creation failed"));
        System.out.println("\n✗ Test 5 - Null Price Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createProduct_nullCategory_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ProductFactory.createProduct(
                        "Burger",
                        new BigDecimal("45.99"),
                        null,
                        50
                )
        );
        assertTrue(ex.getMessage().contains("Product creation failed"));
        System.out.println("\n✗ Test 6 - Null Category Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }
}

