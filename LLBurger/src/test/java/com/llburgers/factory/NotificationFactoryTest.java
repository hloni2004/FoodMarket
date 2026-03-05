package com.llburgers.factory;

import com.llburgers.domain.*;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NotificationFactoryTest {

    private Order order;

    @BeforeEach
    void setUp() {
        Customer customer = UserFactory.createCustomer(
                "Alex Brown", "alex@example.com", "Password123", "0841112222", Block.B, "220"
        );
        order = OrderFactory.createOrder(customer, new BigDecimal("220.00"), Block.B, "220");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createNotification_validOrderNotification_returnsNotificationWithCorrectAttributes() {
        Notification notification = NotificationFactory.createNotification(
                order,
                NotificationType.ORDER_PLACED
        );

        assertNotNull(notification);
        assertEquals(order, notification.getOrder());
        assertEquals(NotificationType.ORDER_PLACED, notification.getType());
        // timestamp is populated by Hibernate at persist time, null in unit tests
        assertNull(notification.getTimestamp());
        System.out.println("\n✓ Test 1 - Valid Order Notification:");
        System.out.println(notification);
    }

    @Test
    void createBusinessNotification_validInput_returnsNotificationWithoutOrder() {
        Notification notification = NotificationFactory.createBusinessNotification(
                NotificationType.BUSINESS_OPENED
        );

        assertNotNull(notification);
        assertNull(notification.getOrder());
        assertEquals(NotificationType.BUSINESS_OPENED, notification.getType());
        System.out.println("\n✓ Test 2 - Business Opened Notification:");
        System.out.println(notification);
    }

    @Test
    void createOrderNotification_validInput_returnsOrderNotification() {
        Notification notification = NotificationFactory.createOrderNotification(
                order,
                NotificationType.ORDER_STATUS_CHANGED
        );

        assertNotNull(notification);
        assertEquals(order, notification.getOrder());
        assertEquals(NotificationType.ORDER_STATUS_CHANGED, notification.getType());
        System.out.println("\n✓ Test 3 - Order Status Changed Notification:");
        System.out.println(notification);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createNotification_nullType_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                NotificationFactory.createNotification(
                        order,
                        null
                )
        );
        assertTrue(ex.getMessage().contains("Notification creation failed"));
        System.out.println("\n✗ Test 4 - Null Notification Type Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrderNotification_nullOrder_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                NotificationFactory.createOrderNotification(
                        null,
                        NotificationType.ORDER_PLACED
                )
        );
        assertTrue(ex.getMessage().contains("OrderNotification creation failed"));
        System.out.println("\n✗ Test 5 - Null Order Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOrderNotification_nullType_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                NotificationFactory.createOrderNotification(
                        order,
                        null
                )
        );
        assertTrue(ex.getMessage().contains("OrderNotification creation failed"));
        System.out.println("\n✗ Test 6 - Null Type Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }
}

