package com.llburgers.factory;

import com.llburgers.domain.*;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemSideFactoryTest {

    private OrderItem orderItem;
    private Side side;

    @BeforeEach
    void setUp() {
        Customer customer = UserFactory.createCustomer(
                "Sarah Williams", "sarah@example.com", "Password123", "0835551234", Block.A, "115"
        );
        Order order = OrderFactory.createOrder(customer, new BigDecimal("180.00"), Block.A, "115");
        Product product = ProductFactory.createProduct(
                "Classic Burger",
                new BigDecimal("55.00"),
                ProductCategory.BURGER,
                40
        );
        orderItem = OrderItemFactory.createOrderItem(order, product, 1, new BigDecimal("55.00"));
        side = SideFactory.createSide("French Fries", new BigDecimal("12.50"), 200);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createOrderItemSide_validInput_returnsOrderItemSideWithCorrectAttributes() {
        OrderItemSide orderItemSide = OrderItemSideFactory.createOrderItemSide(
                orderItem,
                side,
                1
        );

        assertNotNull(orderItemSide);
        assertEquals(orderItem, orderItemSide.getOrderItem());
        assertEquals(side, orderItemSide.getSide());
        assertEquals(1, orderItemSide.getQuantity());
        System.out.println("\n✓ Test 1 - Valid OrderItemSide Creation:");
        System.out.println(orderItemSide);
    }

    @Test
    void createOrderItemSide_multipleSides_areCreatedCorrectly() {
        OrderItemSide orderItemSide = OrderItemSideFactory.createOrderItemSide(
                orderItem,
                side,
                3
        );

        assertNotNull(orderItemSide);
        assertEquals(3, orderItemSide.getQuantity());
        System.out.println("\n✓ Test 2 - OrderItemSide with Multiple Quantity:");
        System.out.println(orderItemSide);
    }

    @Test
    void createOrderItemSide_differentSides_areCreatedCorrectly() {
        Side coleslaw = SideFactory.createSide("Coleslaw", new BigDecimal("6.00"), 150);
        OrderItemSide orderItemSide = OrderItemSideFactory.createOrderItemSide(
                orderItem,
                coleslaw,
                2
        );

        assertNotNull(orderItemSide);
        assertEquals(coleslaw, orderItemSide.getSide());
        assertEquals(2, orderItemSide.getQuantity());
        System.out.println("\n✓ Test 3 - OrderItemSide with Different Side:");
        System.out.println(orderItemSide);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createOrderItemSide_nullOrderItem_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderItemSideFactory.createOrderItemSide(
                        null,
                        side,
                        1
                )
        );
        assertTrue(ex.getMessage().contains("OrderItemSide creation failed"));
        System.out.println("\n✗ Test 4 - Null OrderItem Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrderItemSide_nullSide_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderItemSideFactory.createOrderItemSide(
                        orderItem,
                        null,
                        1
                )
        );
        assertTrue(ex.getMessage().contains("OrderItemSide creation failed"));
        System.out.println("\n✗ Test 5 - Null Side Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrderItemSide_negativeQuantity_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderItemSideFactory.createOrderItemSide(
                        orderItem,
                        side,
                        -1
                )
        );
        assertTrue(ex.getMessage().contains("OrderItemSide creation failed"));
        System.out.println("\n✗ Test 6 - Negative Quantity Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }
}

