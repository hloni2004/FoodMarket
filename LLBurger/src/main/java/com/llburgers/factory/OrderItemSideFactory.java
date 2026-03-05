package com.llburgers.factory;

import com.llburgers.domain.OrderItem;
import com.llburgers.domain.OrderItemSide;
import com.llburgers.domain.Side;
import com.llburgers.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class OrderItemSideFactory {

    /**
     * Creates a validated OrderItemSide object.
     *
     * @param orderItem the associated order item
     * @param side      the side to add
     * @param quantity  quantity of the side (must be > 0)
     * @return a new OrderItemSide instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static OrderItemSide createOrderItemSide(OrderItem orderItem, Side side, int quantity) {
        List<String> errors = new ArrayList<>();

        // Validate order item
        if (orderItem == null) {
            errors.add("OrderItem cannot be null");
        }

        // Validate side
        if (side == null) {
            errors.add("Side cannot be null");
        }

        // Validate quantity
        if (quantity <= 0) {
            errors.add("Quantity must be greater than zero");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("OrderItemSide creation failed — " + String.join("; ", errors));
        }

        return OrderItemSide.builder()
                .orderItem(orderItem)
                .side(side)
                .quantity(quantity)
                .build();
    }
}

