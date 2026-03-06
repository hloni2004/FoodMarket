package com.llburgers.service.impl;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatus;
import com.llburgers.domain.BusinessStatusLog;
import com.llburgers.event.BusinessClosedEvent;
import com.llburgers.event.BusinessOpenedEvent;
import com.llburgers.repository.BusinessStatusLogRepository;
import com.llburgers.repository.BusinessStatusRepository;
import com.llburgers.service.IBusinessStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BusinessStatusServiceImpl implements IBusinessStatusService {

    private static final Logger log = LoggerFactory.getLogger(BusinessStatusServiceImpl.class);

    private final BusinessStatusRepository repository;
    private final BusinessStatusLogRepository logRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BusinessStatusServiceImpl(BusinessStatusRepository repository,
                                     BusinessStatusLogRepository logRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.logRepository = logRepository;
        this.eventPublisher = eventPublisher;
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

        // Publish event — listener broadcasts emails to all active customers
        eventPublisher.publishEvent(new BusinessOpenedEvent(admin));

        log.info("[BUSINESS-OPENED] by admin={}", admin.getId());
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

        // Publish event — listener broadcasts emails to all active customers
        eventPublisher.publishEvent(new BusinessClosedEvent(admin, closedMessage));

        log.info("[BUSINESS-CLOSED] by admin={}, message='{}'", admin.getId(), closedMessage);
        return saved;
    }
}

