package com.llburgers.factory;

import com.llburgers.domain.Order;
import com.llburgers.domain.OrderItem;
import com.llburgers.domain.Product;
import com.llburgers.util.Helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderItemFactory {

    /**
     * Creates a validated OrderItem object.
     *
     * @param order     the associated order
     * @param product   the product in the order
     * @param quantity  quantity of the product (must be > 0)
     * @param totalPrice total price for this item (must be > 0)
     * @return a new OrderItem instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static OrderItem createOrderItem(Order order, Product product, int quantity, BigDecimal totalPrice) {
        List<String> errors = new ArrayList<>();

        // Validate order
        if (order == null) {
            errors.add("Order cannot be null");
        }

        // Validate product
        if (product == null) {
            errors.add("Product cannot be null");
        }

        // Validate quantity
        if (quantity <= 0) {
            errors.add("Quantity must be greater than zero");
        }

        // Validate total price
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Total price must be greater than zero");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("OrderItem creation failed — " + String.join("; ", errors));
        }

        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .totalPrice(totalPrice)
                .build();
    }
}

