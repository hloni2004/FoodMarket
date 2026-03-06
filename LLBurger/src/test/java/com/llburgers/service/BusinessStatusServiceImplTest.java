package com.llburgers.service;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatus;
import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.domain.enums.Role;
import com.llburgers.repository.AdminRepository;
import com.llburgers.repository.BusinessStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class BusinessStatusServiceImplTest {

    @Autowired
    private IBusinessStatusService businessStatusService;

    @Autowired
    private BusinessStatusRepository businessStatusRepository;

    @Autowired
    private AdminRepository adminRepository;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = adminRepository.save(Admin.builder()
                .name("Status Admin")
                .email("status.admin." + UUID.randomUUID() + "@llburgers.com")
                .password("statusPass99")
                .role(Role.ADMIN)
                .adminLevel(AdminLevel.SUPER_ADMIN)
                .active(true)
                .build());

        // Ensure the singleton BusinessStatus row exists
        businessStatusService.getCurrentStatus();
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – getCurrentStatus() always returns a non-null BusinessStatus")
    void getCurrentStatus_returnsNonNull() {
        BusinessStatus status = businessStatusService.getCurrentStatus();

        assertNotNull(status);
        assertEquals(1L, status.getId());
    }

    @Test
    @DisplayName("PASS – openBusiness() sets isOpen to true in the database")
    void openBusiness_setsOpenTrue() {
        BusinessStatus status = businessStatusService.openBusiness(admin);

        assertTrue(status.isOpen());
        BusinessStatus fromDb = businessStatusRepository.findById(1L).orElseThrow();
        assertTrue(fromDb.isOpen());
    }

    @Test
    @DisplayName("PASS – closeBusiness() sets isOpen to false and stores the message")
    void closeBusiness_setsOpenFalseWithMessage() {
        businessStatusService.openBusiness(admin);

        BusinessStatus status = businessStatusService.closeBusiness(admin, "Closed for tonight", null);

        assertFalse(status.isOpen());
        assertEquals("Closed for tonight", status.getClosedMessage());
        BusinessStatus fromDb = businessStatusRepository.findById(1L).orElseThrow();
        assertFalse(fromDb.isOpen());
    }

    @Test
    @DisplayName("PASS – isOpen() reflects the current persisted state")
    void isOpen_reflectsCurrentState() {
        businessStatusService.openBusiness(admin);
        assertTrue(businessStatusService.isOpen());

        businessStatusService.closeBusiness(admin, "Night close", null);
        assertFalse(businessStatusService.isOpen());
    }

    @Test
    @DisplayName("PASS – closeBusiness() persists expectedReopenAt when provided")
    void closeBusiness_persistsExpectedReopenAt() {
        LocalDateTime reopenAt = LocalDateTime.now().plusHours(3);
        BusinessStatus status = businessStatusService.closeBusiness(admin, "Back soon", reopenAt);

        assertNotNull(status.getExpectedReopenAt());
        assertTrue(status.getExpectedReopenAt().isAfter(LocalDateTime.now()));
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – asserts business is open right after closing it (intentionally wrong)")
    void closeBusiness_assertsStillOpen_fail() {
        businessStatusService.closeBusiness(admin, "Closed", null);

        assertTrue(businessStatusService.isOpen(),
                "Intentionally wrong — business was just closed; this should FAIL");
    }

    @Test
    @DisplayName("FAIL – asserts closedMessage is null after calling closeBusiness (intentionally wrong)")
    void closeBusiness_assertsNullMessage_fail() {
        BusinessStatus status = businessStatusService.closeBusiness(admin, "Back at 6pm", null);

        assertNull(status.getClosedMessage(),
                "Intentionally wrong — closedMessage was set; this should FAIL");
    }
}
