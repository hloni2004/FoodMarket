package com.llburgers.factory;

import com.llburgers.domain.Notification;
import com.llburgers.domain.Order;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import com.llburgers.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class NotificationFactory {

    /**
     * Creates a validated Notification object.
     *
     * Notifications can be order-specific or business-level.
     * For business-level notifications (e.g., BUSINESS_OPENED, BUSINESS_CLOSED),
     * pass null as the order parameter.
     *
     * @param order  the associated order (nullable for business-level notifications)
     * @param type   the notification type
     * @return a new Notification instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Notification createNotification(Order order, NotificationType type) {
        List<String> errors = new ArrayList<>();

        // Validate notification type
        if (type == null) {
            errors.add("Notification type cannot be null");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Notification creation failed — " + String.join("; ", errors));
        }

        return Notification.builder()
                .order(order)  // Can be null for business-level notifications
                .type(type)
                .status(NotificationStatus.PENDING)
                .build();
    }

    /**
     * Creates a business-level notification (no associated order).
     *
     * @param type the notification type
     * @return a new Notification instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Notification createBusinessNotification(NotificationType type) {
        return createNotification(null, type);
    }

    /**
     * Creates an order-specific notification.
     *
     * @param order the associated order
     * @param type  the notification type
     * @return a new Notification instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Notification createOrderNotification(Order order, NotificationType type) {
        List<String> errors = new ArrayList<>();

        if (order == null) {
            errors.add("Order cannot be null for order-specific notifications");
        }

        if (type == null) {
            errors.add("Notification type cannot be null");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("OrderNotification creation failed — " + String.join("; ", errors));
        }

        return Notification.builder()
                .order(order)
                .type(type)
                .status(NotificationStatus.PENDING)
                .build();
    }
}

