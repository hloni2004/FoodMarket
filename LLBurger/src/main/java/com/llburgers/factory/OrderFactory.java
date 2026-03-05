package com.llburgers.factory;

import com.llburgers.domain.Customer;
import com.llburgers.domain.Order;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.OrderStatus;
import com.llburgers.util.Helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderFactory {

    /**
     * Creates a validated Order object.
     *
     * @param customer               the customer placing the order
     * @param totalPrice             total order price (must be > 0)
     * @param deliveryBlock          delivery block (A, B, or C)
     * @param deliveryRoomNumber     delivery room number
     * @param specialInstructions    special instructions (optional)
     * @return a new Order instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Order createOrder(Customer customer, BigDecimal totalPrice, Block deliveryBlock,
                                   String deliveryRoomNumber, String specialInstructions) {
        List<String> errors = new ArrayList<>();

        // Validate customer
        if (customer == null) {
            errors.add("Customer cannot be null");
        }

        // Validate total price
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Total price must be greater than zero");
        }

        // Validate delivery block
        if (deliveryBlock == null) {
            errors.add("Delivery block cannot be null");
        }

        // Validate delivery room number
        if (!Helper.isValidRoomNumber(deliveryRoomNumber)) {
            errors.add("Delivery room number is invalid (must not be empty and cannot exceed 20 characters)");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Order creation failed — " + String.join("; ", errors));
        }

        return Order.builder()
                .customer(customer)
                .totalPrice(totalPrice)
                .deliveryBlock(deliveryBlock)
                .deliveryRoomNumber(deliveryRoomNumber.trim())
                .specialInstructions(specialInstructions != null && !Helper.isNullOrEmpty(specialInstructions)
                        ? specialInstructions.trim() : null)
                .status(OrderStatus.PROCESSING)
                .build();
    }

    /**
     * Creates an Order without special instructions.
     *
     * @param customer           the customer placing the order
     * @param totalPrice         total order price
     * @param deliveryBlock      delivery block
     * @param deliveryRoomNumber delivery room number
     * @return a new Order instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Order createOrder(Customer customer, BigDecimal totalPrice, Block deliveryBlock,
                                   String deliveryRoomNumber) {
        return createOrder(customer, totalPrice, deliveryBlock, deliveryRoomNumber, null);
    }
}

