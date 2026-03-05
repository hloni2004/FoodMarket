package com.llburgers.factory;

import com.llburgers.domain.Admin;
import com.llburgers.domain.Customer;
import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserFactoryTest {

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS — createAdmin
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createAdmin_validInput_returnsAdminWithCorrectAttributes() {
        Admin admin = UserFactory.createAdmin(
                "Jane Doe", "jane@example.com", "Secret12", "0821234567", AdminLevel.ADMIN
        );

        assertNotNull(admin);
        assertEquals("Jane Doe", admin.getName());
        assertEquals("jane@example.com", admin.getEmail());
        assertEquals("Secret12", admin.getPassword());
        assertEquals("0821234567", admin.getPhone());
        assertEquals(AdminLevel.ADMIN, admin.getAdminLevel());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isActive());
        System.out.println("\n✓ Test 1 - Valid Admin Creation:");
        System.out.println("  Name        : " + admin.getName());
        System.out.println("  Email       : " + admin.getEmail());
        System.out.println("  Phone       : " + admin.getPhone());
        System.out.println("  Admin Level : " + admin.getAdminLevel());
        System.out.println("  Role        : " + admin.getRole());
        System.out.println("  Active      : " + admin.isActive());
    }

    @Test
    void createAdmin_emailIsStoredInLowerCase() {
        Admin admin = UserFactory.createAdmin(
                "Jane Doe", "JANE@EXAMPLE.COM", "Secret12", null, AdminLevel.ADMIN
        );
        assertEquals("jane@example.com", admin.getEmail());
        System.out.println("\n✓ Test 2 - Admin Email Stored in Lowercase:");
        System.out.println("  Input Email  : JANE@EXAMPLE.COM");
        System.out.println("  Stored Email : " + admin.getEmail());
    }

    @Test
    void createAdmin_nullPhone_isAccepted() {
        Admin admin = UserFactory.createAdmin(
                "Jane Doe", "jane@example.com", "Secret12", null, AdminLevel.ADMIN
        );
        assertNull(admin.getPhone());
        System.out.println("\n✓ Test 3 - Admin with Null Phone:");
        System.out.println("  Name  : " + admin.getName());
        System.out.println("  Phone : " + admin.getPhone());
    }

    @Test
    void createAdmin_superAdminLevel_isSetCorrectly() {
        Admin admin = UserFactory.createAdmin(
                "Jane Doe", "jane@example.com", "Secret12", null, AdminLevel.SUPER_ADMIN
        );
        assertEquals(AdminLevel.SUPER_ADMIN, admin.getAdminLevel());
        System.out.println("\n✓ Test 4 - Admin with SUPER_ADMIN Level:");
        System.out.println("  Name        : " + admin.getName());
        System.out.println("  Admin Level : " + admin.getAdminLevel());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS — createAdmin (exceptions, all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createAdmin_invalidName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin("J", "jane@example.com", "Secret12", null, AdminLevel.ADMIN)
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
        System.out.println("\n✗ Test 5 - Invalid Admin Name Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createAdmin_nullName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(null, "jane@example.com", "Secret12", null, AdminLevel.ADMIN)
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        System.out.println("\n✗ Test 6 - Null Admin Name Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createAdmin_invalidEmail_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin("Jane Doe", "not-an-email", "Secret12", null, AdminLevel.ADMIN)
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        System.out.println("\n✗ Test 7 - Invalid Admin Email Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createAdmin_nullEmail_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin("Jane Doe", null, "Secret12", null, AdminLevel.ADMIN)
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        System.out.println("\n✗ Test 8 - Null Admin Email Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createAdmin_passwordTooShort_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin("Jane Doe", "jane@example.com", "abc", null, AdminLevel.ADMIN)
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
        System.out.println("\n✗ Test 9 - Short Admin Password Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createAdmin_nullPassword_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin("Jane Doe", "jane@example.com", null, null, AdminLevel.ADMIN)
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        System.out.println("\n✗ Test 10 - Null Admin Password Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createAdmin_invalidPhone_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin("Jane Doe", "jane@example.com", "Secret12", "not-a-phone", AdminLevel.ADMIN)
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("phone"));
        System.out.println("\n✗ Test 11 - Invalid Admin Phone Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createAdmin_nullAdminLevel_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin("Jane Doe", "jane@example.com", "Secret12", null, null)
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("admin level"));
        System.out.println("\n✗ Test 12 - Null Admin Level Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createAdmin_multipleInvalidFields_errorMessageContainsAllIssues() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin("J", "bad-email", "123", "bad-phone", AdminLevel.ADMIN)
        );
        String msg = ex.getMessage();
        assertTrue(msg.toLowerCase().contains("name"));
        assertTrue(msg.toLowerCase().contains("email"));
        assertTrue(msg.toLowerCase().contains("password"));
        assertTrue(msg.toLowerCase().contains("phone"));
        System.out.println("\n✗ Test 13 - Multiple Admin Field Errors:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORRECT TESTS — createCustomer
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createCustomer_validInput_returnsCustomerWithCorrectAttributes() {
        Customer customer = UserFactory.createCustomer(
                "John Smith", "john@example.com", "Pass1234", "0831234567", Block.B, "101"
        );

        assertNotNull(customer);
        assertEquals("John Smith", customer.getName());
        assertEquals("john@example.com", customer.getEmail());
        assertEquals("Pass1234", customer.getPassword());
        assertEquals("0831234567", customer.getPhone());
        assertEquals(Block.B, customer.getBlock());
        assertEquals("101", customer.getRoomNumber());
        assertEquals(Role.CUSTOMER, customer.getRole());
        assertTrue(customer.isActive());
        System.out.println("\n✓ Test 14 - Valid Customer Creation:");
        System.out.println("  Name        : " + customer.getName());
        System.out.println("  Email       : " + customer.getEmail());
        System.out.println("  Phone       : " + customer.getPhone());
        System.out.println("  Block       : " + customer.getBlock());
        System.out.println("  Room        : " + customer.getRoomNumber());
        System.out.println("  Role        : " + customer.getRole());
        System.out.println("  Active      : " + customer.isActive());
    }

    @Test
    void createCustomer_emailIsStoredInLowerCase() {
        Customer customer = UserFactory.createCustomer(
                "John Smith", "JOHN@EXAMPLE.COM", "Pass1234", null, Block.B, "202"
        );
        assertEquals("john@example.com", customer.getEmail());
        System.out.println("\n✓ Test 15 - Customer Email Stored in Lowercase:");
        System.out.println("  Input Email  : JOHN@EXAMPLE.COM");
        System.out.println("  Stored Email : " + customer.getEmail());
    }

    @Test
    void createCustomer_nullPhone_isAccepted() {
        Customer customer = UserFactory.createCustomer(
                "John Smith", "john@example.com", "Pass1234", null, Block.C, "303"
        );
        assertNull(customer.getPhone());
        System.out.println("\n✓ Test 16 - Customer with Null Phone:");
        System.out.println("  Name  : " + customer.getName());
        System.out.println("  Block : " + customer.getBlock());
        System.out.println("  Room  : " + customer.getRoomNumber());
        System.out.println("  Phone : " + customer.getPhone());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INCORRECT TESTS — createCustomer (exceptions, all pass)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createCustomer_invalidName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer("J", "john@example.com", "Pass1234", null, Block.B, "101")
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
        System.out.println("\n✗ Test 17 - Invalid Customer Name Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createCustomer_invalidEmail_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer("John Smith", "not-an-email", "Pass1234", null, Block.B, "101")
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        System.out.println("\n✗ Test 18 - Invalid Customer Email Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createCustomer_nullBlock_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer("John Smith", "john@example.com", "Pass1234", null, null, "101")
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("block"));
        System.out.println("\n✗ Test 19 - Null Customer Block Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createCustomer_nullRoomNumber_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer("John Smith", "john@example.com", "Pass1234", null, Block.B, null)
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("room"));
        System.out.println("\n✗ Test 20 - Null Customer Room Number Exception:");
        System.out.println("  Exception: " + ex.getMessage());
    }

    @Test
    void createCustomer_multipleInvalidFields_errorMessageContainsAllIssues() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer("J", "bad-email", "123", "bad-phone", null, null)
        );
        String msg = ex.getMessage();
        assertTrue(msg.toLowerCase().contains("name"));
        assertTrue(msg.toLowerCase().contains("email"));
        assertTrue(msg.toLowerCase().contains("password"));
        assertTrue(msg.toLowerCase().contains("phone"));
        assertTrue(msg.toLowerCase().contains("block"));
        assertTrue(msg.toLowerCase().contains("room"));
        System.out.println("\n✗ Test 21 - Multiple Customer Field Errors:");
        System.out.println("  Exception: " + ex.getMessage());
    }
}