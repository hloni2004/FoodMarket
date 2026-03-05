package com.llburgers.factory;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatusLog;
import com.llburgers.util.Helper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BusinessStatusLogFactory {

    /**
     * Creates a validated BusinessStatusLog object.
     *
     * This immutable audit log records every time an admin toggles the business open/closed.
     *
     * @param isOpen          the new state (true = opened, false = closed)
     * @param closedMessage   closed message at time of toggle (optional, relevant if closing)
     * @param expectedReopenAt expected reopen time (optional, relevant if closing)
     * @param changedBy       the admin who performed the toggle
     * @return a new BusinessStatusLog instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static BusinessStatusLog createBusinessStatusLog(boolean isOpen, String closedMessage,
                                                           LocalDateTime expectedReopenAt, Admin changedBy) {
        List<String> errors = new ArrayList<>();

        // Validate admin
        if (changedBy == null) {
            errors.add("Admin who made the change cannot be null");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("BusinessStatusLog creation failed — " + String.join("; ", errors));
        }

        return BusinessStatusLog.builder()
                .open(isOpen)
                .closedMessage(closedMessage != null && !Helper.isNullOrEmpty(closedMessage) ? closedMessage.trim() : null)
                .expectedReopenAt(expectedReopenAt)
                .changedBy(changedBy)
                .build();
    }

    /**
     * Creates a BusinessStatusLog when the business is opened.
     *
     * @param changedBy admin who opened the business
     * @return a new open BusinessStatusLog instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static BusinessStatusLog createOpenLog(Admin changedBy) {
        return createBusinessStatusLog(true, null, null, changedBy);
    }

    /**
     * Creates a BusinessStatusLog when the business is closed.
     *
     * @param closedMessage    message for customers
     * @param expectedReopenAt expected reopen time (optional)
     * @param changedBy        admin who closed the business
     * @return a new closed BusinessStatusLog instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static BusinessStatusLog createClosedLog(String closedMessage, LocalDateTime expectedReopenAt,
                                                    Admin changedBy) {
        return createBusinessStatusLog(false, closedMessage, expectedReopenAt, changedBy);
    }
}

