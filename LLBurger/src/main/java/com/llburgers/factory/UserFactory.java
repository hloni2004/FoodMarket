package com.llburgers.factory;

import com.llburgers.domain.Admin;
import com.llburgers.domain.Customer;
import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.Role;
import com.llburgers.util.Helper;

import java.util.ArrayList;
import java.util.List;


public class UserFactory {


    public static Admin createAdmin(String name, String email, String password,
                                    String phone, AdminLevel adminLevel) {
        List<String> errors = new ArrayList<>();

        // Validate shared User attributes via Helper
        if (!Helper.isValidName(name)) {
            errors.add("Name is invalid (2–50 letters, spaces, hyphens or apostrophes)");
        }
        if (!Helper.isValidEmail(email)) {
            errors.add("Email format is invalid");
        }
        if (!Helper.isValidPassword(password)) {
            errors.add("Password must be at least 8 characters");
        }
        if (phone != null && !Helper.isNullOrEmpty(phone) && !Helper.isValidPhone(phone)) {
            errors.add("Phone number format is invalid");
        }

        // Validate Admin-specific attributes via Helper
        if (Helper.isNullOrEmpty(String.valueOf(adminLevel))) {
            errors.add("Admin level cannot be null");
        }
        if (adminLevel == null) {
            errors.add("Admin level cannot be null");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Admin creation failed — " + String.join("; ", errors));
        }

        return Admin.builder()
                .name(name.trim())
                .email(email.trim().toLowerCase())
                .password(password)
                .phone(phone != null && !Helper.isNullOrEmpty(phone) ? phone.trim() : null)
                .adminLevel(adminLevel)
                .role(Role.ADMIN)
                .active(true)
                .build();
    }

    /**
     * Creates a validated Customer object.
     *
     * @param name       full name
     * @param email      unique email address
     * @param password   password (min 8 characters)
     * @param phone      phone number (optional — pass null to skip)
     * @param block      residential block (A, B, or C)
     * @param roomNumber room number within the block
     * @return a new Customer instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static Customer createCustomer(String name, String email, String password,
                                          String phone, Block block, String roomNumber) {
        List<String> errors = new ArrayList<>();

        // Validate shared User attributes via Helper
        if (!Helper.isValidName(name)) {
            errors.add("Name is invalid (2–50 letters, spaces, hyphens or apostrophes)");
        }
        if (!Helper.isValidEmail(email)) {
            errors.add("Email format is invalid");
        }
        if (!Helper.isValidPassword(password)) {
            errors.add("Password must be at least 8 characters");
        }
        if (phone != null && !Helper.isNullOrEmpty(phone) && !Helper.isValidPhone(phone)) {
            errors.add("Phone number format is invalid");
        }

        // Validate Customer-specific attributes via Helper
        if (block == null) {
            errors.add("Block cannot be null");
        }
        if (!Helper.isValidRoomNumber(roomNumber)) {
            errors.add("Room number is invalid (must not be empty and cannot exceed 20 characters)");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Customer creation failed — " + String.join("; ", errors));
        }

        return Customer.builder()
                .name(name.trim())
                .email(email.trim().toLowerCase())
                .password(password)
                .phone(phone != null && !Helper.isNullOrEmpty(phone) ? phone.trim() : null)
                .block(block)
                .roomNumber(roomNumber.trim())
                .role(Role.CUSTOMER)
                .active(true)
                .build();
    }
}
