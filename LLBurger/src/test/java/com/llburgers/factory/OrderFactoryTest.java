package com.llburgers.factory;

import com.llburgers.domain.Customer;
import com.llburgers.domain.Order;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderFactoryTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = UserFactory.createCustomer(
                "John Doe", "john@example.com", "Password123", "0821234567", Block.A, "101"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createOrder_validInput_returnsOrderWithCorrectAttributes() {
        Order order = OrderFactory.createOrder(
                customer,
                new BigDecimal("150.00"),
                Block.A,
                "101",
                "No onions please!"
        );

        assertNotNull(order);
        assertEquals(customer, order.getCustomer());
        assertEquals(new BigDecimal("150.00"), order.getTotalPrice());
        assertEquals(Block.A, order.getDeliveryBlock());
        assertEquals("101", order.getDeliveryRoomNumber());
        assertEquals("No onions please!", order.getSpecialInstructions());
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        System.out.println("\n✓ Test 1 - Valid Order Creation:");
        System.out.println(order);
    }

    @Test
    void createOrder_withoutSpecialInstructions_returnsOrderWithNullInstructions() {
        Order order = OrderFactory.createOrder(
                customer,
                new BigDecimal("85.50"),
                Block.B,
                "205"
        );

        assertNotNull(order);
        assertEquals(customer, order.getCustomer());
        assertNull(order.getSpecialInstructions());
        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        System.out.println("\n✓ Test 2 - Order without Special Instructions:");
        System.out.println(order);
    }

    @Test
    void createOrder_differentBlocks_areCreatedCorrectly() {
        Order orderBlockC = OrderFactory.createOrder(
                customer,
                new BigDecimal("120.00"),
                Block.C,
                "312"
        );

        assertNotNull(orderBlockC);
        assertEquals(Block.C, orderBlockC.getDeliveryBlock());
        assertEquals("312", orderBlockC.getDeliveryRoomNumber());
        System.out.println("\n✓ Test 3 - Order with Different Block:");
        System.out.println(orderBlockC);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createOrder_nullCustomer_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderFactory.createOrder(
                        null,
                        new BigDecimal("150.00"),
                        Block.A,
                        "101"
                )
        );
        assertTrue(ex.getMessage().contains("Order creation failed"));
        System.out.println("\n✗ Test 4 - Null Customer Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrder_invalidPrice_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderFactory.createOrder(
                        customer,
                        BigDecimal.ZERO,
                        Block.A,
                        "101"
                )
        );
        assertTrue(ex.getMessage().contains("Order creation failed"));
        System.out.println("\n✗ Test 5 - Invalid Price Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrder_nullDeliveryBlock_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                OrderFactory.createOrder(
                        customer,
                        new BigDecimal("150.00"),
                        null,
                        "101"
                )
        );
        assertTrue(ex.getMessage().contains("Order creation failed"));
        System.out.println("\n✗ Test 6 - Null Delivery Block Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }
}

