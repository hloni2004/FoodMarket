package com.llburgers.factory;

import com.llburgers.domain.*;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemFactoryTest {

    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        Customer customer = UserFactory.createCustomer(
                "Jane Smith", "jane@example.com", "Password123", "0827654321", Block.B, "202"
        );
        order = OrderFactory.createOrder(customer, new BigDecimal("200.00"), Block.B, "202");
        product = ProductFactory.createProduct(
                "Premium Burger",
                new BigDecimal("65.00"),
                ProductCategory.BURGER,
                30
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createOrderItem_validInput_returnsOrderItemWithCorrectAttributes() {
        OrderItem orderItem = OrderItemFactory.createOrderItem(
                order,
                product,
                2,
                new BigDecimal("130.00")
        );

        assertNotNull(orderItem);
        assertEquals(order, orderItem.getOrder());
        assertEquals(product, orderItem.getProduct());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(new BigDecimal("130.00"), orderItem.getTotalPrice());
        System.out.println("\n✓ Test 1 - Valid OrderItem Creation:");
        System.out.println(orderItem);
    }

    @Test
    void createOrderItem_singleQuantity_isCreatedCorrectly() {
        OrderItem orderItem = OrderItemFactory.createOrderItem(
                order,
                product,
                1,
                new BigDecimal("65.00")
        );

        assertNotNull(orderItem);
        assertEquals(1, orderItem.getQuantity());
        assertEquals(new BigDecimal("65.00"), orderItem.getTotalPrice());
        System.out.println("\n✓ Test 2 - OrderItem with Single Quantity:");
        System.out.println(orderItem);
    }

    @Test
    void createOrderItem_largeQuantity_isCreatedCorrectly() {
        OrderItem orderItem = OrderItemFactory.createOrderItem(
                order,
                product,
                10,
                new BigDecimal("650.00")
        );

        assertNotNull(orderItem);
        assertEquals(10, orderItem.getQuantity());
        System.out.println("\n✓ Test 3 - OrderItem with Large Quantity:");
        System.out.println(orderItem);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createOrderItem_nullOrder_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderItemFactory.createOrderItem(
                        null,
                        product,
                        2,
                        new BigDecimal("130.00")
                )
        );
        assertTrue(ex.getMessage().contains("OrderItem creation failed"));
        System.out.println("\n✗ Test 4 - Null Order Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrderItem_zeroQuantity_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderItemFactory.createOrderItem(
                        order,
                        product,
                        0,
                        new BigDecimal("0.00")
                )
        );
        assertTrue(ex.getMessage().contains("OrderItem creation failed"));
        System.out.println("\n✗ Test 5 - Zero Quantity Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrderItem_invalidTotalPrice_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderItemFactory.createOrderItem(
                        order,
                        product,
                        2,
                        new BigDecimal("-50.00")
                )
        );
        assertTrue(ex.getMessage().contains("OrderItem creation failed"));
        System.out.println("\n✗ Test 6 - Invalid Total Price Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }
}

