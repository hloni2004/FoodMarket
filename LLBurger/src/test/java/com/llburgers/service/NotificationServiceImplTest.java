package com.llburgers.service;

import com.llburgers.domain.Notification;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import com.llburgers.repository.NotificationRepository;
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
class NotificationServiceImplTest {

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification savedNotification;

    @BeforeEach
    void setUp() {
        // Business-level notification — no order required (order is nullable)
        savedNotification = notificationRepository.save(Notification.builder()
                .type(NotificationType.BUSINESS_OPENED)
                .status(NotificationStatus.PENDING)
                .build());
    }

    // ─── PASSING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("PASS – create() persists a notification and generates an ID")
    void create_persistsNotificationWithGeneratedId() {
        Notification n = Notification.builder()
                .type(NotificationType.BUSINESS_CLOSED)
                .status(NotificationStatus.PENDING)
                .build();

        Notification created = notificationService.create(n);

        assertNotNull(created.getId());
        assertTrue(notificationRepository.existsById(created.getId()));
        assertEquals(NotificationType.BUSINESS_CLOSED, created.getType());
    }

    @Test
    @DisplayName("PASS – read() returns the correct notification by ID")
    void read_returnsCorrectNotification() {
        Notification fetched = notificationService.read(savedNotification.getId());

        assertEquals(savedNotification.getId(), fetched.getId());
        assertEquals(NotificationType.BUSINESS_OPENED, fetched.getType());
        assertEquals(NotificationStatus.PENDING, fetched.getStatus());
    }

    @Test
    @DisplayName("PASS – markAsSent() updates status to SENT in the database")
    void markAsSent_updatesStatusToSent() {
        Notification result = notificationService.markAsSent(savedNotification.getId());

        assertEquals(NotificationStatus.SENT, result.getStatus());
        Notification fromDb = notificationRepository.findById(savedNotification.getId()).orElseThrow();
        assertEquals(NotificationStatus.SENT, fromDb.getStatus());
    }

    @Test
    @DisplayName("PASS – markAsFailed() updates status to FAILED in the database")
    void markAsFailed_updatesStatusToFailed() {
        Notification result = notificationService.markAsFailed(savedNotification.getId());

        assertEquals(NotificationStatus.FAILED, result.getStatus());
        Notification fromDb = notificationRepository.findById(savedNotification.getId()).orElseThrow();
        assertEquals(NotificationStatus.FAILED, fromDb.getStatus());
    }

    @Test
    @DisplayName("PASS – findByType() only returns notifications of the requested type")
    void findByType_returnsMatchingType() {
        List<Notification> results = notificationService.findByType(NotificationType.BUSINESS_OPENED);

        assertTrue(results.stream().allMatch(n -> n.getType() == NotificationType.BUSINESS_OPENED));
        assertTrue(results.stream().anyMatch(n -> n.getId().equals(savedNotification.getId())));
    }

    @Test
    @DisplayName("PASS – findByStatus(PENDING) returns only pending notifications")
    void findByStatus_returnsPendingNotifications() {
        List<Notification> pending = notificationService.findByStatus(NotificationStatus.PENDING);

        assertTrue(pending.stream().allMatch(n -> n.getStatus() == NotificationStatus.PENDING));
        assertTrue(pending.stream().anyMatch(n -> n.getId().equals(savedNotification.getId())));
    }

    @Test
    @DisplayName("PASS – findBusinessNotifications() returns only notifications with no order")
    void findBusinessNotifications_returnsOrderlessNotifications() {
        List<Notification> results = notificationService.findBusinessNotifications();

        assertTrue(results.stream().allMatch(n -> n.getOrder() == null));
        assertTrue(results.stream().anyMatch(n -> n.getId().equals(savedNotification.getId())));
    }

    // ─── FAILING TESTS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("FAIL – read() with unknown ID expects no exception (intentionally wrong)")
    void read_unknownId_expectsNoException_fail() {
        assertDoesNotThrow(() -> notificationService.read(UUID.randomUUID()),
                "Intentionally wrong — NotificationServiceImpl throws; this should FAIL");
    }

    @Test
    @DisplayName("FAIL – asserts status is SENT immediately after create() (intentionally wrong)")
    void create_assertsStatusSent_fail() {
        Notification n = notificationService.create(Notification.builder()
                .type(NotificationType.BUSINESS_OPENED)
                .status(NotificationStatus.PENDING)
                .build());

        assertEquals(NotificationStatus.SENT, n.getStatus(),
                "Intentionally wrong — status is PENDING on creation; this should FAIL");
    }
}
