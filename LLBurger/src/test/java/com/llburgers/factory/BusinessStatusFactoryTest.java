package com.llburgers.factory;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatus;
import com.llburgers.domain.enums.AdminLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BusinessStatusFactoryTest {

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = UserFactory.createAdmin(
                "Admin User", "admin@example.com", "AdminPass123", "0860001111", AdminLevel.SUPER_ADMIN
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createBusinessStatus_validClosedStatus_returnsBusinessStatusWithCorrectAttributes() {
        LocalDateTime reopenTime = LocalDateTime.now().plusHours(2);
        BusinessStatus status = BusinessStatusFactory.createClosedStatus(
                "Back in 2 hours!",
                reopenTime,
                admin
        );

        assertNotNull(status);
        assertEquals(1L, status.getId());
        assertFalse(status.isOpen());
        assertEquals("Back in 2 hours!", status.getClosedMessage());
        assertEquals(reopenTime, status.getExpectedReopenAt());
        assertEquals(admin, status.getLastChangedBy());
        System.out.println("\n✓ Test 1 - Valid Closed Status Creation:");
        System.out.println(status);
    }

    @Test
    void createBusinessStatus_openStatus_returnsOpenStatus() {
        BusinessStatus status = BusinessStatusFactory.createOpenStatus(admin);

        assertNotNull(status);
        assertEquals(1L, status.getId());
        assertTrue(status.isOpen());
        assertNull(status.getClosedMessage());
        assertNull(status.getExpectedReopenAt());
        System.out.println("\n✓ Test 2 - Open Status:");
        System.out.println(status);
    }

    @Test
    void createBusinessStatus_closedWithoutReopenTime_isCreatedCorrectly() {
        BusinessStatus status = BusinessStatusFactory.createClosedStatus(
                "Closed for maintenance",
                null,
                admin
        );

        assertNotNull(status);
        assertFalse(status.isOpen());
        assertEquals("Closed for maintenance", status.getClosedMessage());
        assertNull(status.getExpectedReopenAt());
        System.out.println("\n✓ Test 3 - Closed Status without Reopen Time:");
        System.out.println(status);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createBusinessStatus_nullAdmin_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BusinessStatusFactory.createBusinessStatus(
                        false,
                        "Back soon!",
                        null,
                        null
                )
        );
        assertTrue(ex.getMessage().contains("BusinessStatus creation failed"));
        System.out.println("\n✗ Test 4 - Null Admin Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createBusinessStatus_withInvalidClosedMessage_throwsException() {
        // This test demonstrates that empty/whitespace messages could be caught
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BusinessStatusFactory.createBusinessStatus(
                        false,
                        "   ",
                        null,
                        null
                )
        );
        assertTrue(ex.getMessage().contains("BusinessStatus creation failed"));
        System.out.println("\n✗ Test 5 - Invalid Closed Message Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createClosedStatus_nullAdmin_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BusinessStatusFactory.createClosedStatus(
                        "Closed",
                        LocalDateTime.now().plusHours(1),
                        null
                )
        );
        assertTrue(ex.getMessage().contains("BusinessStatus creation failed"));
        System.out.println("\n✗ Test 6 - Null Admin in Closed Status Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }
}

