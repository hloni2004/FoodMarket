package com.llburgers.factory;

import com.llburgers.domain.Side;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SideFactoryTest {

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createSide_validInputWithImage_returnsSideWithCorrectAttributes() {
        Side side = SideFactory.createSide(
                "Mashed Potatoes",
                new BigDecimal("8.50"),
                150,
                "https://vlklxrrgvaoykpkvdoxe.supabase.co/storage/v1/object/public/llburger-images/sides/mashed-potatoes.jpg",
                true
        );

        assertNotNull(side);
        assertEquals("Mashed Potatoes", side.getName());
        assertEquals(new BigDecimal("8.50"), side.getPrice());
        assertEquals(150, side.getStockQuantity());
        assertNotNull(side.getImageUrl());
        assertTrue(side.getImageUrl().contains("sides/mashed-potatoes.jpg"));
        assertTrue(side.isAvailability());
        System.out.println("\n✓ Test 1 - Valid Side Creation (with image):");
        System.out.println("  Name       : " + side.getName());
        System.out.println("  Price      : R" + side.getPrice());
        System.out.println("  Stock      : " + side.getStockQuantity());
        System.out.println("  Image URL  : " + side.getImageUrl());
        System.out.println("  Available  : " + side.isAvailability());
    }

    @Test
    void createSide_withDefaultAvailability_returnsSideWithTrueAvailabilityAndNullImage() {
        Side side = SideFactory.createSide(
                "Coleslaw",
                new BigDecimal("5.99"),
                75
        );

        assertNotNull(side);
        assertEquals("Coleslaw", side.getName());
        assertTrue(side.isAvailability());
        assertNull(side.getImageUrl());
        System.out.println("\n✓ Test 2 - Side with Default Availability (no image):");
        System.out.println("  Name       : " + side.getName());
        System.out.println("  Price      : R" + side.getPrice());
        System.out.println("  Stock      : " + side.getStockQuantity());
        System.out.println("  Image URL  : " + side.getImageUrl());
        System.out.println("  Available  : " + side.isAvailability());
    }

    @Test
    void createSide_zeroStock_isValid() {
        Side side = SideFactory.createSide(
                "Out of Stock Side",
                new BigDecimal("7.00"),
                0,
                "https://vlklxrrgvaoykpkvdoxe.supabase.co/storage/v1/object/public/llburger-images/sides/out-of-stock.jpg",
                false
        );

        assertNotNull(side);
        assertEquals(0, side.getStockQuantity());
        assertFalse(side.isAvailability());
        assertNotNull(side.getImageUrl());
        System.out.println("\n✓ Test 3 - Side with Zero Stock:");
        System.out.println("  Name       : " + side.getName());
        System.out.println("  Price      : R" + side.getPrice());
        System.out.println("  Stock      : " + side.getStockQuantity());
        System.out.println("  Image URL  : " + side.getImageUrl());
        System.out.println("  Available  : " + side.isAvailability());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createSide_nullName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                SideFactory.createSide(
                        null,
                        new BigDecimal("8.50"),
                        100,
                        null,
                        true
                )
        );
        assertTrue(ex.getMessage().contains("Side creation failed"));
        System.out.println("\n✗ Test 4 - Null Name Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createSide_nullPrice_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                SideFactory.createSide(
                        "Fries",
                        null,
                        100,
                        null,
                        true
                )
        );
        assertTrue(ex.getMessage().contains("Side creation failed"));
        System.out.println("\n✗ Test 5 - Null Price Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createSide_negativeStockQuantity_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                SideFactory.createSide(
                        "Salad",
                        new BigDecimal("6.50"),
                        -5,
                        null,
                        true
                )
        );
        assertTrue(ex.getMessage().contains("Side creation failed"));
        System.out.println("\n✗ Test 6 - Negative Stock Quantity Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }
}
