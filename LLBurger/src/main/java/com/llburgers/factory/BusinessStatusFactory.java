package com.llburgers.factory;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatus;
import com.llburgers.util.Helper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BusinessStatusFactory {

    /**
     * Creates a validated BusinessStatus object (singleton).
     *
     * This method ensures only one BusinessStatus record exists with ID = 1.
     *
     * @param isOpen              whether the business is open (true) or closed (false)
     * @param closedMessage       message shown to customers when closed (optional)
     * @param expectedReopenAt    expected time to reopen (optional)
     * @param lastChangedBy       admin who last toggled the status
     * @return a new BusinessStatus instance with ID = 1
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static BusinessStatus createBusinessStatus(boolean isOpen, String closedMessage,
                                                      LocalDateTime expectedReopenAt, Admin lastChangedBy) {
        List<String> errors = new ArrayList<>();

        // Validate admin
        if (lastChangedBy == null) {
            errors.add("Admin who made the change cannot be null");
        }

        // Validate closed message — if provided it must not be blank
        if (!isOpen && closedMessage != null && Helper.isNullOrEmpty(closedMessage)) {
            errors.add("Closed message cannot be blank");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("BusinessStatus creation failed — " + String.join("; ", errors));
        }

        return BusinessStatus.builder()
                .id(1L)  // Always singleton with ID = 1
                .open(isOpen)
                .closedMessage(closedMessage != null && !Helper.isNullOrEmpty(closedMessage) ? closedMessage.trim() : null)
                .expectedReopenAt(expectedReopenAt)
                .lastChangedBy(lastChangedBy)
                .build();
    }

    /**
     * Creates a BusinessStatus when the business is opened.
     *
     * @param lastChangedBy admin who opened the business
     * @return a new open BusinessStatus instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static BusinessStatus createOpenStatus(Admin lastChangedBy) {
        return createBusinessStatus(true, null, null, lastChangedBy);
    }

    /**
     * Creates a BusinessStatus when the business is closed.
     *
     * @param closedMessage      message for customers
     * @param expectedReopenAt   expected reopen time (optional)
     * @param lastChangedBy      admin who closed the business
     * @return a new closed BusinessStatus instance
     * @throws IllegalArgumentException if any attribute is invalid
     */
    public static BusinessStatus createClosedStatus(String closedMessage, LocalDateTime expectedReopenAt,
                                                    Admin lastChangedBy) {
        return createBusinessStatus(false, closedMessage, expectedReopenAt, lastChangedBy);
    }
}

