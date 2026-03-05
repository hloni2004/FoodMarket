package com.llburgers.factory;

import com.llburgers.domain.Extra;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExtraFactoryTest {

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createExtra_validInputWithImage_returnsExtraWithCorrectAttributes() {
        Extra extra = ExtraFactory.createExtra(
                "Bacon Strips",
                new BigDecimal("3.50"),
                200,
                "https://vlklxrrgvaoykpkvdoxe.supabase.co/storage/v1/object/public/llburger-images/extras/bacon.jpg",
                true
        );

        assertNotNull(extra);
        assertEquals("Bacon Strips", extra.getName());
        assertEquals(new BigDecimal("3.50"), extra.getPrice());
        assertEquals(200, extra.getStockQuantity());
        assertNotNull(extra.getImageUrl());
        assertTrue(extra.getImageUrl().contains("extras/bacon.jpg"));
        assertTrue(extra.isAvailability());
        System.out.println("\n✓ Test 1 - Valid Extra Creation (with image):");
        System.out.println("  Name       : " + extra.getName());
        System.out.println("  Price      : R" + extra.getPrice());
        System.out.println("  Stock      : " + extra.getStockQuantity());
        System.out.println("  Image URL  : " + extra.getImageUrl());
        System.out.println("  Available  : " + extra.isAvailability());
    }

    @Test
    void createExtra_withDefaultAvailabilityAndImage_returnsExtraWithTrueAvailability() {
        Extra extra = ExtraFactory.createExtra(
                "Cheese Slice",
                new BigDecimal("2.00"),
                300,
                "https://vlklxrrgvaoykpkvdoxe.supabase.co/storage/v1/object/public/llburger-images/extras/cheese.jpg"
        );

        assertNotNull(extra);
        assertEquals("Cheese Slice", extra.getName());
        assertTrue(extra.isAvailability());
        assertEquals(300, extra.getStockQuantity());
        assertNotNull(extra.getImageUrl());
        assertTrue(extra.getImageUrl().contains("extras/cheese.jpg"));
        System.out.println("\n✓ Test 2 - Extra with Default Availability & Image:");
        System.out.println("  Name       : " + extra.getName());
        System.out.println("  Price      : R" + extra.getPrice());
        System.out.println("  Stock      : " + extra.getStockQuantity());
        System.out.println("  Image URL  : " + extra.getImageUrl());
        System.out.println("  Available  : " + extra.isAvailability());
    }

    @Test
    void createExtra_noImage_returnsExtraWithNullImageUrl() {
        Extra extra = ExtraFactory.createExtra(
                "Truffle Oil",
                new BigDecimal("15.99"),
                5,
                false
        );

        assertNotNull(extra);
        assertFalse(extra.isAvailability());
        assertEquals("Truffle Oil", extra.getName());
        assertNull(extra.getImageUrl());
        System.out.println("\n✓ Test 3 - Extra without Image (null imageUrl):");
        System.out.println("  Name       : " + extra.getName());
        System.out.println("  Price      : R" + extra.getPrice());
        System.out.println("  Stock      : " + extra.getStockQuantity());
        System.out.println("  Image URL  : " + extra.getImageUrl());
        System.out.println("  Available  : " + extra.isAvailability());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createExtra_emptyName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ExtraFactory.createExtra(
                        "",
                        new BigDecimal("2.00"),
                        100,
                        "https://example.com/extra.jpg",
                        true
                )
        );
        assertTrue(ex.getMessage().contains("Extra creation failed"));
        System.out.println("\n✗ Test 4 - Empty Name Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createExtra_negativePrice_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ExtraFactory.createExtra(
                        "Sauce",
                        new BigDecimal("-1.50"),
                        100,
                        null,
                        true
                )
        );
        assertTrue(ex.getMessage().contains("Extra creation failed"));
        System.out.println("\n✗ Test 5 - Negative Price Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createExtra_negativeStock_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ExtraFactory.createExtra(
                        "Pickles",
                        new BigDecimal("1.50"),
                        -10,
                        null,
                        true
                )
        );
        assertTrue(ex.getMessage().contains("Extra creation failed"));
        System.out.println("\n✗ Test 6 - Negative Stock Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }
}
