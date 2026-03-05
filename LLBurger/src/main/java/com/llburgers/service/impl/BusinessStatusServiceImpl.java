package com.llburgers.service.impl;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatus;
import com.llburgers.domain.BusinessStatusLog;
import com.llburgers.domain.Customer;
import com.llburgers.domain.Notification;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import com.llburgers.repository.BusinessStatusLogRepository;
import com.llburgers.repository.BusinessStatusRepository;
import com.llburgers.repository.CustomerRepository;
import com.llburgers.repository.NotificationRepository;
import com.llburgers.service.IBusinessStatusService;
import com.llburgers.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BusinessStatusServiceImpl implements IBusinessStatusService {

    private static final Logger log = LoggerFactory.getLogger(BusinessStatusServiceImpl.class);

    private final BusinessStatusRepository repository;
    private final BusinessStatusLogRepository logRepository;
    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final IEmailService emailService;

    public BusinessStatusServiceImpl(BusinessStatusRepository repository,
                                     BusinessStatusLogRepository logRepository,
                                     CustomerRepository customerRepository,
                                     NotificationRepository notificationRepository,
                                     IEmailService emailService) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    public BusinessStatus getCurrentStatus() {
        return repository.findCurrent()
                .orElseGet(() -> {
                    BusinessStatus initial = BusinessStatus.builder()
                            .id(1L)
                            .open(false)
                            .build();
                    return repository.save(initial);
                });
    }

    @Override
    public boolean isOpen() {
        return getCurrentStatus().isOpen();
    }

    // ─── Business Logic ───────────────────────────────────────────────────────

    @Transactional
    @Override
    public BusinessStatus openBusiness(Admin admin) {
        BusinessStatus status = getCurrentStatus();
        status.setOpen(true);
        status.setClosedMessage(null);
        status.setExpectedReopenAt(null);
        status.setLastChangedBy(admin);
        BusinessStatus saved = repository.save(status);

        // Audit log
        logRepository.save(BusinessStatusLog.builder()
                .open(true)
                .changedBy(admin)
                .build());

        // Notify all active customers
        List<Customer> activeCustomers = customerRepository.findByActive(true);
        for (Customer customer : activeCustomers) {
            Notification notification = Notification.builder()
                    .type(NotificationType.BUSINESS_OPENED)
                    .status(NotificationStatus.PENDING)
                    .build();
            notificationRepository.save(notification);

            try {
                emailService.sendBusinessOpenedEmail(customer);
                notification.setStatus(NotificationStatus.SENT);
            } catch (Exception e) {
                log.error("[BUSINESS] Failed to send open email to {}: {}",
                        customer.getEmail(), e.getMessage());
                notification.setStatus(NotificationStatus.FAILED);
            }
            notificationRepository.save(notification);
        }

        log.info("[BUSINESS-OPENED] by admin={}, notified {} customers",
                admin.getId(), activeCustomers.size());
        return saved;
    }

    @Transactional
    @Override
    public BusinessStatus closeBusiness(Admin admin, String closedMessage, LocalDateTime expectedReopenAt) {
        BusinessStatus status = getCurrentStatus();
        status.setOpen(false);
        status.setClosedMessage(closedMessage);
        status.setExpectedReopenAt(expectedReopenAt);
        status.setLastChangedBy(admin);
        BusinessStatus saved = repository.save(status);

        // Audit log
        logRepository.save(BusinessStatusLog.builder()
                .open(false)
                .closedMessage(closedMessage)
                .expectedReopenAt(expectedReopenAt)
                .changedBy(admin)
                .build());

        // Notify all active customers
        List<Customer> activeCustomers = customerRepository.findByActive(true);
        for (Customer customer : activeCustomers) {
            Notification notification = Notification.builder()
                    .type(NotificationType.BUSINESS_CLOSED)
                    .status(NotificationStatus.PENDING)
                    .build();
            notificationRepository.save(notification);

            try {
                emailService.sendBusinessClosedEmail(customer, closedMessage);
                notification.setStatus(NotificationStatus.SENT);
            } catch (Exception e) {
                log.error("[BUSINESS] Failed to send close email to {}: {}",
                        customer.getEmail(), e.getMessage());
                notification.setStatus(NotificationStatus.FAILED);
            }
            notificationRepository.save(notification);
        }

        log.info("[BUSINESS-CLOSED] by admin={}, message='{}', notified {} customers",
                admin.getId(), closedMessage, activeCustomers.size());
        return saved;
    }
}

