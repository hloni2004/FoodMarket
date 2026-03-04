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
    // createAdmin
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
        System.out.print(admin);
    }

    @Test
    void createAdmin_emailIsStoredInLowerCase() {
        Admin admin = UserFactory.createAdmin(
                "Jane Doe", "JANE@EXAMPLE.COM", "Secret12", null, AdminLevel.ADMIN
        );
        assertEquals("jane@example.com", admin.getEmail());
    }

    @Test
    void createAdmin_nullPhone_isAccepted() {
        Admin admin = UserFactory.createAdmin(
                "Jane Doe", "jane@example.com", "Secret12", null, AdminLevel.ADMIN
        );
        assertNull(admin.getPhone());
    }

    @Test
    void createAdmin_superAdminLevel_isSetCorrectly() {
        Admin admin = UserFactory.createAdmin(
                "Jane Doe", "jane@example.com", "Secret12", null, AdminLevel.SUPER_ADMIN
        );
        assertEquals(AdminLevel.SUPER_ADMIN, admin.getAdminLevel());
    }

    @Test
    void createAdmin_invalidName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(
                        "J", "jane@example.com", "Secret12", null, AdminLevel.ADMIN
                )
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    void createAdmin_nullName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(
                        null, "jane@example.com", "Secret12", null, AdminLevel.ADMIN
                )
        );
    }

    @Test
    void createAdmin_invalidEmail_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(
                        "Jane Doe", "not-an-email", "Secret12", null, AdminLevel.ADMIN
                )
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void createAdmin_nullEmail_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(
                        "Jane Doe", null, "Secret12", null, AdminLevel.ADMIN
                )
        );
    }

    @Test
    void createAdmin_passwordTooShort_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(
                        "Jane Doe", "jane@example.com", "abc", null, AdminLevel.ADMIN
                )
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void createAdmin_nullPassword_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(
                        "Jane Doe", "jane@example.com", null, null, AdminLevel.ADMIN
                )
        );
    }

    @Test
    void createAdmin_invalidPhone_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(
                        "Jane Doe", "jane@example.com", "Secret12", "not-a-phone", AdminLevel.ADMIN
                )
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("phone"));
    }

    @Test
    void createAdmin_nullAdminLevel_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(
                        "Jane Doe", "jane@example.com", "Secret12", null, null
                )
        );
        assertTrue(ex.getMessage().contains("Admin creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("admin level"));
    }

    @Test
    void createAdmin_multipleInvalidFields_errorMessageContainsAllIssues() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createAdmin(
                        "J", "bad-email", "123", "bad-phone", AdminLevel.ADMIN
                )
        );
        String msg = ex.getMessage();
        assertTrue(msg.toLowerCase().contains("name"));
        assertTrue(msg.toLowerCase().contains("email"));
        assertTrue(msg.toLowerCase().contains("password"));
        assertTrue(msg.toLowerCase().contains("phone"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createCustomer
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createCustomer_validInput_returnsCustomerWithCorrectAttributes() {
        Customer customer = UserFactory.createCustomer(
                "John Smith", "john@example.com", "Pass1234", "0831234567", Block.A, "101"
        );

        assertNotNull(customer);
        assertEquals("John Smith", customer.getName());
        assertEquals("john@example.com", customer.getEmail());
        assertEquals("Pass1234", customer.getPassword());
        assertEquals("0831234567", customer.getPhone());
        assertEquals(Block.A, customer.getBlock());
        assertEquals("101", customer.getRoomNumber());
        assertEquals(Role.CUSTOMER, customer.getRole());
        assertTrue(customer.isActive());
    }

    @Test
    void createCustomer_emailIsStoredInLowerCase() {
        Customer customer = UserFactory.createCustomer(
                "John Smith", "JOHN@EXAMPLE.COM", "Pass1234", null, Block.B, "202"
        );
        assertEquals("john@example.com", customer.getEmail());
    }

    @Test
    void createCustomer_nullPhone_isAccepted() {
        Customer customer = UserFactory.createCustomer(
                "John Smith", "john@example.com", "Pass1234", null, Block.C, "303"
        );
        assertNull(customer.getPhone());
    }

    @Test
    void createCustomer_allBlockValues_areAccepted() {
        for (Block block : Block.values()) {
            Customer customer = UserFactory.createCustomer(
                    "John Smith", "john" + block + "@example.com", "Pass1234", null, block, "101"
            );
            assertEquals(block, customer.getBlock());
        }
    }

    @Test
    void createCustomer_invalidName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "J", "john@example.com", "Pass1234", null, Block.A, "101"
                )
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    void createCustomer_nullName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        null, "john@example.com", "Pass1234", null, Block.A, "101"
                )
        );
    }

    @Test
    void createCustomer_invalidEmail_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "John Smith", "not-an-email", "Pass1234", null, Block.A, "101"
                )
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void createCustomer_nullEmail_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "John Smith", null, "Pass1234", null, Block.A, "101"
                )
        );
    }

    @Test
    void createCustomer_passwordTooShort_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "John Smith", "john@example.com", "abc", null, Block.A, "101"
                )
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void createCustomer_nullPassword_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "John Smith", "john@example.com", null, null, Block.A, "101"
                )
        );
    }

    @Test
    void createCustomer_invalidPhone_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "John Smith", "john@example.com", "Pass1234", "not-a-phone", Block.A, "101"
                )
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("phone"));
    }

    @Test
    void createCustomer_nullBlock_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "John Smith", "john@example.com", "Pass1234", null, null, "101"
                )
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("block"));
    }

    @Test
    void createCustomer_nullRoomNumber_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "John Smith", "john@example.com", "Pass1234", null, Block.A, null
                )
        );
        assertTrue(ex.getMessage().contains("Customer creation failed"));
        assertTrue(ex.getMessage().toLowerCase().contains("room"));
    }

    @Test
    void createCustomer_emptyRoomNumber_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "John Smith", "john@example.com", "Pass1234", null, Block.A, "   "
                )
        );
    }

    @Test
    void createCustomer_roomNumberExceeds20Chars_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "John Smith", "john@example.com", "Pass1234", null, Block.A, "123456789012345678901"
                )
        );
    }

    @Test
    void createCustomer_multipleInvalidFields_errorMessageContainsAllIssues() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                UserFactory.createCustomer(
                        "J", "bad-email", "123", "bad-phone", null, null
                )
        );
        String msg = ex.getMessage();
        assertTrue(msg.toLowerCase().contains("name"));
        assertTrue(msg.toLowerCase().contains("email"));
        assertTrue(msg.toLowerCase().contains("password"));
        assertTrue(msg.toLowerCase().contains("phone"));
        assertTrue(msg.toLowerCase().contains("block"));
        assertTrue(msg.toLowerCase().contains("room"));
    }
}