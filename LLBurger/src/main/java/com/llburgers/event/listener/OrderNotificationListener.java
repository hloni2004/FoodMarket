package com.llburgers.event.listener;

import com.llburgers.event.OrderCancelledEvent;
import com.llburgers.event.OrderPlacedEvent;
import com.llburgers.event.OrderStatusChangedEvent;
import com.llburgers.messaging.RabbitMQPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges Spring's in-process order events to RabbitMQ.
 *
 * <p>Each listener fires <em>after the originating transaction commits</em>
 * ({@code TransactionPhase.AFTER_COMMIT}) to guarantee the order row is
 * visible in the database before the queue message is sent.
 * Actual email sending and notification creation are handled by
 * {@link com.llburgers.messaging.consumer.OrderMessageConsumer}.</p>
 */
@Component
public class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);

    private final RabbitMQPublisher publisher;

    public OrderNotificationListener(RabbitMQPublisher publisher) {
        this.publisher = publisher;
    }

    // ─── Order Placed ─────────────────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("[EVENT→MQ] OrderPlacedEvent – publishing to queue, orderId={}",
                event.order().getId());
        publisher.publishOrderPlaced(
                event.order().getId(),
                event.affectedProductIds(),
                event.affectedExtraIds(),
                event.affectedSideIds());
    }

    // ─── Order Status Changed ─────────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("[EVENT→MQ] OrderStatusChangedEvent – publishing to queue, orderId={}, {} → {}",
                event.order().getId(), event.previousStatus(), event.newStatus());
        publisher.publishOrderStatusChanged(
                event.order().getId(),
                event.previousStatus().name(),
                event.newStatus().name());
    }

    // ─── Order Cancelled ──────────────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("[EVENT→MQ] OrderCancelledEvent – publishing to queue, orderId={}",
                event.order().getId());
        publisher.publishOrderCancelled(event.order().getId());
    }
}
