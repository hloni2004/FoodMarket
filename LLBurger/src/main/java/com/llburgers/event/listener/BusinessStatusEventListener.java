package com.llburgers.event.listener;

import com.llburgers.event.BusinessClosedEvent;
import com.llburgers.event.BusinessOpenedEvent;
import com.llburgers.messaging.RabbitMQPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges Spring's in-process business-status events to RabbitMQ.
 *
 * <p>Each listener fires after the business-status transaction commits
 * ({@code TransactionPhase.AFTER_COMMIT}). The actual email broadcasts
 * to active customers are handled by
 * {@link com.llburgers.messaging.consumer.BusinessStatusMessageConsumer}.</p>
 */
@Component
public class BusinessStatusEventListener {

    private static final Logger log = LoggerFactory.getLogger(BusinessStatusEventListener.class);

    private final RabbitMQPublisher publisher;

    public BusinessStatusEventListener(RabbitMQPublisher publisher) {
        this.publisher = publisher;
    }

    // ─── Business Opened ──────────────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBusinessOpened(BusinessOpenedEvent event) {
        log.info("[EVENT→MQ] BusinessOpenedEvent – publishing to queue, adminId={}",
                event.admin().getId());
        publisher.publishBusinessOpened(
                event.admin().getId(),
                event.admin().getName());
    }

    // ─── Business Closed ──────────────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBusinessClosed(BusinessClosedEvent event) {
        log.info("[EVENT→MQ] BusinessClosedEvent – publishing to queue, adminId={}",
                event.admin().getId());
        publisher.publishBusinessClosed(
                event.admin().getId(),
                event.admin().getName(),
                event.closedMessage());
    }
}
