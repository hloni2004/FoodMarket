package com.llburgers.dto;

import com.llburgers.domain.Customer;
import com.llburgers.domain.User;
import com.llburgers.domain.enums.Block;

import java.util.UUID;

/**
 * Safe, password-free user profile sent back to the client after auth.
 */
public record UserSummary(
    UUID   id,
    String name,
    String email,
    String phone,
    String role,
    Block  block,
    String roomNumber,
    String paymentMethods
) {
    public static UserSummary from(User user) {
        Block  block          = null;
        String roomNumber     = null;
        String paymentMethods = null;

        if (user instanceof Customer c) {
            block          = c.getBlock();
            roomNumber     = c.getRoomNumber();
            paymentMethods = c.getPaymentMethods();
        }

        return new UserSummary(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getEffectiveRole().name(),
            block,
            roomNumber,
            paymentMethods
        );
    }
}
