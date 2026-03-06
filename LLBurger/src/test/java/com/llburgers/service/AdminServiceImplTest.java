package com.llburgers.service;

import com.llburgers.domain.Admin;
import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.domain.enums.Role;
import com.llburgers.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class AdminServiceImplTest {

    @Autowired
    private IAdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    private Admin savedAdmin;

    @BeforeEach
    void setUp() {
        savedAdmin = adminRepository.save(Admin.builder()
                .name("Test Admin")
                .email("admin." + UUID.randomUUID() + "@llburgers.com")
                .password("adminPass99")
                .role(Role.ADMIN)
                .adminLevel(AdminLevel.ADMIN)
                .active(true)
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() persists an admin and returns a generated ID")
    void create_persistsAdminWithGeneratedId() {
        Admin a = Admin.builder()
                .name("New Admin")
                .email("new.admin." + UUID.randomUUID() + "@llburgers.com")
                .password("newAdminPass1")
                .role(Role.ADMIN)
                .adminLevel(AdminLevel.SUPER_ADMIN)
                .active(true)
                .build();

        Admin created = adminService.create(a);

        assertNotNull(created.getId());
        assertTrue(adminRepository.existsById(created.getId()));
        assertEquals(AdminLevel.SUPER_ADMIN, created.getAdminLevel());
    }

    @Test
    @DisplayName("PASS – read() returns the correct admin by ID")
    void read_returnsCorrectAdmin() {
        Admin fetched = adminService.read(savedAdmin.getId());

        assertEquals(savedAdmin.getId(), fetched.getId());
        assertEquals(savedAdmin.getEmail(), fetched.getEmail());
    }

    @Test
    @DisplayName("PASS – findByEmail() returns the admin matching the given email")
    void findByEmail_returnsMatchingAdmin() {
        Admin fetched = adminService.findByEmail(savedAdmin.getEmail());

        assertEquals(savedAdmin.getId(), fetched.getId());
    }

    @Test
    @DisplayName("PASS – updateAdminLevel() persists the new admin level")
    void updateAdminLevel_persistsNewLevel() {
        Admin updated = adminService.updateAdminLevel(savedAdmin.getId(), AdminLevel.SUPER_ADMIN);

        assertEquals(AdminLevel.SUPER_ADMIN, updated.getAdminLevel());
        Admin fromDb = adminRepository.findById(savedAdmin.getId()).orElseThrow();
        assertEquals(AdminLevel.SUPER_ADMIN, fromDb.getAdminLevel());
    }

    @Test
    @DisplayName("PASS – deactivate() persists active=false to the database")
    void deactivate_persistsActiveFalse() {
        Admin deactivated = adminService.deactivate(savedAdmin.getId());

        assertFalse(deactivated.isActive());
        Admin fromDb = adminRepository.findById(savedAdmin.getId()).orElseThrow();
        assertFalse(fromDb.isActive());
    }

    @Test
    @DisplayName("PASS – findByAdminLevel() returns only admins at the requested level")
    void findByAdminLevel_returnsMatchingAdmins() {
        List<Admin> admins = adminService.findByAdminLevel(AdminLevel.ADMIN);

        assertTrue(admins.stream().allMatch(a -> a.getAdminLevel() == AdminLevel.ADMIN));
        assertTrue(admins.stream().anyMatch(a -> a.getId().equals(savedAdmin.getId())));
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – read() with unknown ID expects no exception (intentionally wrong)")
    void read_unknownId_expectsNoException_fail() {
        assertDoesNotThrow(() -> adminService.read(UUID.randomUUID()),
                "Intentionally wrong — AdminServiceImpl throws; this should FAIL");
    }

    @Test
    @DisplayName("FAIL – deactivate() asserts admin is still active after deactivation (intentionally wrong)")
    void deactivate_assertsStillActive_fail() {
        adminService.deactivate(savedAdmin.getId());
        Admin fromDb = adminRepository.findById(savedAdmin.getId()).orElseThrow();

        assertTrue(fromDb.isActive(),
                "Intentionally wrong — admin was deactivated; this should FAIL");
    }
}
