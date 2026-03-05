package com.llburgers.factory;

import com.llburgers.domain.*;
import com.llburgers.domain.enums.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RatingFactoryTest {

    private Order order;

    @BeforeEach
    void setUp() {
        Customer customer = UserFactory.createCustomer(
                "Emma Davis", "emma@example.com", "Password123", "0853335555", Block.C, "330"
        );
        order = OrderFactory.createOrder(customer, new BigDecimal("175.00"), Block.C, "330");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createRating_validInput_returnsRatingWithCorrectAttributes() {
        Rating rating = RatingFactory.createRating(
                order,
                5,
                4,
                "Excellent food, quick delivery!"
        );

        assertNotNull(rating);
        assertEquals(order, rating.getOrder());
        assertEquals(5, rating.getFoodRating());
        assertEquals(4, rating.getDeliveryRating());
        assertEquals("Excellent food, quick delivery!", rating.getFeedback());
        // createdAt is populated by Hibernate at persist time, null in unit tests
        assertNull(rating.getCreatedAt());
        System.out.println("\n✓ Test 1 - Valid Rating Creation:");
        System.out.println(rating);
    }

    @Test
    void createRating_withoutFeedback_returnsRatingWithNullFeedback() {
        Rating rating = RatingFactory.createRating(
                order,
                4,
                5
        );

        assertNotNull(rating);
        assertEquals(4, rating.getFoodRating());
        assertEquals(5, rating.getDeliveryRating());
        assertNull(rating.getFeedback());
        System.out.println("\n✓ Test 2 - Rating without Feedback:");
        System.out.println(rating);
    }

    @Test
    void createRating_oneStarRating_isCreatedCorrectly() {
        Rating rating = RatingFactory.createRating(
                order,
                1,
                1,
                "Not satisfied with the order"
        );

        assertNotNull(rating);
        assertEquals(1, rating.getFoodRating());
        assertEquals(1, rating.getDeliveryRating());
        System.out.println("\n✓ Test 3 - One Star Rating:");
        System.out.println(rating);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createRating_nullOrder_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                RatingFactory.createRating(
                        null,
                        5,
                        4
                )
        );
        assertTrue(ex.getMessage().contains("Rating creation failed"));
        System.out.println("\n✗ Test 4 - Null Order Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createRating_foodRatingTooHigh_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                RatingFactory.createRating(
                        order,
                        6,
                        4
                )
        );
        assertTrue(ex.getMessage().contains("Rating creation failed"));
        System.out.println("\n✗ Test 5 - Food Rating Too High Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createRating_deliveryRatingTooLow_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                RatingFactory.createRating(
                        order,
                        5,
                        0
                )
        );
        assertTrue(ex.getMessage().contains("Rating creation failed"));
        System.out.println("\n✗ Test 6 - Delivery Rating Too Low Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }
}

