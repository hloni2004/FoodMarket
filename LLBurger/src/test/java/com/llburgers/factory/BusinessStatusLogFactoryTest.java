package com.llburgers.factory;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatusLog;
import com.llburgers.domain.enums.AdminLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BusinessStatusLogFactoryTest {

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = UserFactory.createAdmin(
                "Manager Admin", "manager@example.com", "ManagerPass123", "0862223333", AdminLevel.ADMIN
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS - Should Pass
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createBusinessStatusLog_validClosedLog_returnsLogWithCorrectAttributes() {
        LocalDateTime reopenTime = LocalDateTime.now().plusHours(3);
        BusinessStatusLog log = BusinessStatusLogFactory.createClosedLog(
                "Back at 6 PM!",
                reopenTime,
                admin
        );

        assertNotNull(log);
        assertFalse(log.isOpen());
        assertEquals("Back at 6 PM!", log.getClosedMessage());
        assertEquals(reopenTime, log.getExpectedReopenAt());
        assertEquals(admin, log.getChangedBy());
        // changedAt is populated by Hibernate at persist time, null in unit tests
        assertNull(log.getChangedAt());
        System.out.println("\n✓ Test 1 - Valid Closed Log Creation:");
        System.out.println(log);
    }

    @Test
    void createBusinessStatusLog_openLog_returnsOpenLog() {
        BusinessStatusLog log = BusinessStatusLogFactory.createOpenLog(admin);

        assertNotNull(log);
        assertTrue(log.isOpen());
        assertNull(log.getClosedMessage());
        assertNull(log.getExpectedReopenAt());
        assertEquals(admin, log.getChangedBy());
        System.out.println("\n✓ Test 2 - Open Log:");
        System.out.println(log);
    }

    @Test
    void createBusinessStatusLog_closedLogWithoutReopenTime_isCreatedCorrectly() {
        BusinessStatusLog log = BusinessStatusLogFactory.createClosedLog(
                "Emergency closure",
                null,
                admin
        );

        assertNotNull(log);
        assertFalse(log.isOpen());
        assertEquals("Emergency closure", log.getClosedMessage());
        assertNull(log.getExpectedReopenAt());
        System.out.println("\n✓ Test 3 - Closed Log without Reopen Time:");
        System.out.println(log);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS - Should Throw Exceptions (but all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createBusinessStatusLog_nullAdmin_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BusinessStatusLogFactory.createBusinessStatusLog(
                        false,
                        "Closed",
                        null,
                        null
                )
        );
        assertTrue(ex.getMessage().contains("BusinessStatusLog creation failed"));
        System.out.println("\n✗ Test 4 - Null Admin Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createOpenLog_nullAdmin_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BusinessStatusLogFactory.createOpenLog(null)
        );
        assertTrue(ex.getMessage().contains("BusinessStatusLog creation failed"));
        System.out.println("\n✗ Test 5 - Null Admin in Open Log Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }

    @Test
    void createClosedLog_nullAdmin_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BusinessStatusLogFactory.createClosedLog(
                        "Closed",
                        LocalDateTime.now().plusHours(1),
                        null
                )
        );
        assertTrue(ex.getMessage().contains("BusinessStatusLog creation failed"));
        System.out.println("\n✗ Test 6 - Null Admin in Closed Log Exception:");
        System.out.println("Exception: " + ex.getMessage());
    }
}

