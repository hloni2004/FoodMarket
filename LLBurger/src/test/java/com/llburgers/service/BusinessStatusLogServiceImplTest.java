package com.llburgers.service;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatusLog;
import com.llburgers.domain.enums.AdminLevel;
import com.llburgers.domain.enums.Role;
import com.llburgers.repository.AdminRepository;
import com.llburgers.repository.BusinessStatusLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class BusinessStatusLogServiceImplTest {

    @Autowired
    private IBusinessStatusLogService logService;

    @Autowired
    private BusinessStatusLogRepository logRepository;

    @Autowired
    private AdminRepository adminRepository;

    private Admin admin;
    private BusinessStatusLog savedLog;

    @BeforeEach
    void setUp() {
        admin = adminRepository.save(Admin.builder()
                .name("Log Admin")
                .email("log.admin." + UUID.randomUUID() + "@llburgers.com")
                .password("logPass99")
                .role(Role.ADMIN)
                .adminLevel(AdminLevel.ADMIN)
                .active(true)
                .build());

        savedLog = logRepository.save(BusinessStatusLog.builder()
                .open(true)
                .changedBy(admin)
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() persists a log entry with a generated ID")
    void create_persistsLogWithGeneratedId() {
        BusinessStatusLog log = BusinessStatusLog.builder()
                .open(false)
                .closedMessage("Closing early today")
                .changedBy(admin)
                .build();

        BusinessStatusLog created = logService.create(log);

        assertNotNull(created.getId());
        assertTrue(logRepository.existsById(created.getId()));
        assertFalse(created.isOpen());
        assertEquals("Closing early today", created.getClosedMessage());
    }

    @Test
    @DisplayName("PASS – read() returns the correct log entry by ID")
    void read_returnsCorrectLog() {
        BusinessStatusLog fetched = logService.read(savedLog.getId());

        assertEquals(savedLog.getId(), fetched.getId());
        assertTrue(fetched.isOpen());
    }

    @Test
    @DisplayName("PASS – findByOpen(true) only returns open-state log entries")
    void findByOpen_returnsOnlyOpenEntries() {
        List<BusinessStatusLog> openLogs = logService.findByOpen(true);

        assertTrue(openLogs.stream().allMatch(BusinessStatusLog::isOpen));
        assertTrue(openLogs.stream().anyMatch(l -> l.getId().equals(savedLog.getId())));
    }

    @Test
    @DisplayName("PASS – findByAdminId() returns logs changed by the given admin")
    void findByAdminId_returnsLogsByAdmin() {
        List<BusinessStatusLog> logs = logService.findByAdminId(admin.getId());

        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().allMatch(l -> l.getChangedBy().getId().equals(admin.getId())));
    }

    @Test
    @DisplayName("PASS – findMostRecent() returns the latest log entry")
    void findMostRecent_returnsLatestEntry() {
        Optional<BusinessStatusLog> recent = logService.findMostRecent();

        assertTrue(recent.isPresent());
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – read() with unknown ID expects no exception (intentionally wrong)")
    void read_unknownId_expectsNoException_fail() {
        assertDoesNotThrow(() -> logService.read(UUID.randomUUID()),
                "Intentionally wrong — BusinessStatusLogServiceImpl throws; this should FAIL");
    }

    @Test
    @DisplayName("FAIL – asserts log is closed=true when it was saved as open=true (intentionally wrong)")
    void read_assertsClosedWhenOpen_fail() {
        BusinessStatusLog fetched = logService.read(savedLog.getId());

        assertFalse(fetched.isOpen(),
                "Intentionally wrong — log was saved as open=true; this should FAIL");
    }
}
