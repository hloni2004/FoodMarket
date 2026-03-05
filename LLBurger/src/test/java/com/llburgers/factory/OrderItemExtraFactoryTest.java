package com.llburgers.factory;

import com.llburgers.domain.*;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemExtraFactoryTest {

    private OrderItem orderItem;
    private Extra extra;

    @BeforeEach
    void setUp() {
        Customer customer = UserFactory.createCustomer(
                "Mike Johnson", "mike@example.com", "Password123", "0829876543", Block.C, "303"
        );
        Order order = OrderFactory.createOrder(customer, new BigDecimal("250.00"), Block.C, "303");
        Product product = ProductFactory.createProduct(
                "Deluxe Burger",
                new BigDecimal("70.00"),
                ProductCategory.BURGER,
                25
        );
        orderItem = OrderItemFactory.createOrderItem(order, product, 1, new BigDecimal("70.00"));
        extra = ExtraFactory.createExtra("Extra Cheese", new BigDecimal("5.00"), 100);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createOrderItemExtra_validInput_returnsOrderItemExtraWithCorrectAttributes() {
        OrderItemExtra orderItemExtra = OrderItemExtraFactory.createOrderItemExtra(
                orderItem,
                extra,
                2
        );

        assertNotNull(orderItemExtra);
        assertEquals(orderItem, orderItemExtra.getOrderItem());
        assertEquals(extra, orderItemExtra.getExtra());
        assertEquals(2, orderItemExtra.getQuantity());
        System.out.println("\n✓ Test 1 - Valid OrderItemExtra Creation:");
        System.out.println(orderItemExtra);
    }

    @Test
    void createOrderItemExtra_singleQuantity_isCreatedCorrectly() {
        OrderItemExtra orderItemExtra = OrderItemExtraFactory.createOrderItemExtra(
                orderItem,
                extra,
                1
        );

        assertNotNull(orderItemExtra);
        assertEquals(1, orderItemExtra.getQuantity());
        System.out.println("\n✓ Test 2 - OrderItemExtra with Single Quantity:");
        System.out.println(orderItemExtra);
    }

    @Test
    void createOrderItemExtra_multipleExtras_areCreatedCorrectly() {
        Extra bacon = ExtraFactory.createExtra("Bacon", new BigDecimal("3.00"), 80);
        OrderItemExtra orderItemExtra = OrderItemExtraFactory.createOrderItemExtra(
                orderItem,
                bacon,
                3
        );

        assertNotNull(orderItemExtra);
        assertEquals(bacon, orderItemExtra.getExtra());
        assertEquals(3, orderItemExtra.getQuantity());
        System.out.println("\n✓ Test 3 - OrderItemExtra with Multiple Quantity:");
        System.out.println(orderItemExtra);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createOrderItemExtra_nullOrderItem_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderItemExtraFactory.createOrderItemExtra(
                        null,
                        extra,
                        1
                )
        );
        assertTrue(ex.getMessage().contains("OrderItemExtra creation failed"));
        System.out.println("\n✗ Test 4 - Null OrderItem Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrderItemExtra_nullExtra_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderItemExtraFactory.createOrderItemExtra(
                        orderItem,
                        null,
                        1
                )
        );
        assertTrue(ex.getMessage().contains("OrderItemExtra creation failed"));
        System.out.println("\n✗ Test 5 - Null Extra Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrderItemExtra_zeroQuantity_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderItemExtraFactory.createOrderItemExtra(
                        orderItem,
                        extra,
                        0
                )
        );
        assertTrue(ex.getMessage().contains("OrderItemExtra creation failed"));
        System.out.println("\n✗ Test 6 - Zero Quantity Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }
}

