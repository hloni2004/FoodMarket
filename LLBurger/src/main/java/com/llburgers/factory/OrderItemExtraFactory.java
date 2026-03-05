package com.llburgers.factory;

import com.llburgers.domain.Extra;
import com.llburgers.domain.OrderItem;
import com.llburgers.domain.OrderItemExtra;
import com.llburgers.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class OrderItemExtraFactory {

    /**
     * Creates a validated OrderItemExtra object.
     *
     * @param orderItem the associated order item
     * @param extra     the extra to add
     * @param quantity  quantity of the extra (must be > 0)
     * @return a new OrderItemExtra instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static OrderItemExtra createOrderItemExtra(OrderItem orderItem, Extra extra, int quantity) {
        List<String> errors = new ArrayList<>();

        // Validate order item
        if (orderItem == null) {
            errors.add("OrderItem cannot be null");
        }

        // Validate extra
        if (extra == null) {
            errors.add("Extra cannot be null");
        }

        // Validate quantity
        if (quantity <= 0) {
            errors.add("Quantity must be greater than zero");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("OrderItemExtra creation failed — " + String.join("; ", errors));
        }

        return OrderItemExtra.builder()
                .orderItem(orderItem)
                .extra(extra)
                .quantity(quantity)
                .build();
    }
}

