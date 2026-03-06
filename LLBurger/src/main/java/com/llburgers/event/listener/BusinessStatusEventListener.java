package com.llburgers.event.listener;

import com.llburgers.domain.Customer;
import com.llburgers.domain.Notification;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import com.llburgers.event.BusinessClosedEvent;
import com.llburgers.event.BusinessOpenedEvent;
import com.llburgers.repository.CustomerRepository;
import com.llburgers.repository.NotificationRepository;
import com.llburgers.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * Listens for business open/close events and broadcasts notifications
 * to all active customers via email (Brevo).
 *
 * <p>Runs asynchronously after the originating transaction commits so
 * the business-status change is guaranteed to be persisted first.</p>
 */
@Component
public class BusinessStatusEventListener {

    private static final Logger log = LoggerFactory.getLogger(BusinessStatusEventListener.class);

    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final IEmailService emailService;

    public BusinessStatusEventListener(CustomerRepository customerRepository,
                                       NotificationRepository notificationRepository,
                                       IEmailService emailService) {
        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    // ─── Business Opened ──────────────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBusinessOpened(BusinessOpenedEvent event) {
        log.info("[EVENT] BusinessOpenedEvent received – admin={}", event.admin().getId());

        List<Customer> activeCustomers = customerRepository.findByActive(true);
        int sent = 0, failed = 0;

        for (Customer customer : activeCustomers) {
            Notification notification = notificationRepository.save(Notification.builder()
                    .type(NotificationType.BUSINESS_OPENED)
                    .status(NotificationStatus.PENDING)
                    .build());

            try {
                emailService.sendBusinessOpenedEmail(customer);
                notification.setStatus(NotificationStatus.SENT);
                sent++;
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
                failed++;
                log.error("[EVENT] Business-opened email FAILED for {}: {}",
                        customer.getEmail(), e.getMessage());
            }
            notificationRepository.save(notification);
        }

        log.info("[EVENT] Business-opened broadcast complete: {} sent, {} failed", sent, failed);
    }

    // ─── Business Closed ──────────────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onBusinessClosed(BusinessClosedEvent event) {
        log.info("[EVENT] BusinessClosedEvent received – admin={}", event.admin().getId());

        List<Customer> activeCustomers = customerRepository.findByActive(true);
        String closedMessage = event.closedMessage();
        int sent = 0, failed = 0;

        for (Customer customer : activeCustomers) {
            Notification notification = notificationRepository.save(Notification.builder()
                    .type(NotificationType.BUSINESS_CLOSED)
                    .status(NotificationStatus.PENDING)
                    .build());

            try {
                emailService.sendBusinessClosedEmail(customer, closedMessage);
                notification.setStatus(NotificationStatus.SENT);
                sent++;
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
                failed++;
                log.error("[EVENT] Business-closed email FAILED for {}: {}",
                        customer.getEmail(), e.getMessage());
            }
            notificationRepository.save(notification);
        }

        log.info("[EVENT] Business-closed broadcast complete: {} sent, {} failed", sent, failed);
    }
}
