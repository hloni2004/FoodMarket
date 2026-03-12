package com.llburgers.messaging;

import com.llburgers.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Facade over {@link RabbitTemplate} that publishes domain events as
 * JSON messages to the {@code llburger.exchange} topic exchange.
 *
 * <p>Every public method corresponds to one routing key and wraps the
 * publish call with structured logging so failures are immediately visible.</p>
 */
@Service
public class RabbitMQPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ─── Order Events ─────────────────────────────────────────────────────────

    /**
     * Publishes an {@link OrderPlacedMessage} after a new order is persisted
     * and stock has been reduced.
     */
    public void publishOrderPlaced(UUID orderId,
                                   List<UUID> affectedProductIds,
                                   List<UUID> affectedExtraIds,
                                   List<UUID> affectedSideIds) {
        OrderPlacedMessage message = new OrderPlacedMessage(
                orderId, affectedProductIds, affectedExtraIds, affectedSideIds);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ORDER_PLACED_KEY,
                    message);
            log.info("[MQ-PUBLISH] order.placed → orderId={}", orderId);
        } catch (Exception e) {
            log.error("[MQ-PUBLISH-ERROR] Failed to publish order.placed for orderId={}: {}",
                    orderId, e.getMessage(), e);
        }
    }

    /**
     * Publishes an {@link OrderStatusChangedMessage} when an admin advances
     * an order through the status workflow.
     */
    public void publishOrderStatusChanged(UUID orderId,
                                          String previousStatus,
                                          String newStatus) {
        OrderStatusChangedMessage message = new OrderStatusChangedMessage(
                orderId, previousStatus, newStatus);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ORDER_STATUS_CHANGED_KEY,
                    message);
            log.info("[MQ-PUBLISH] order.status.changed → orderId={}, {} → {}",
                    orderId, previousStatus, newStatus);
        } catch (Exception e) {
            log.error("[MQ-PUBLISH-ERROR] Failed to publish order.status.changed for orderId={}: {}",
                    orderId, e.getMessage(), e);
        }
    }

    /**
     * Publishes an {@link OrderCancelledMessage} when an order is cancelled
     * and stock is restored.
     */
    public void publishOrderCancelled(UUID orderId) {
        OrderCancelledMessage message = new OrderCancelledMessage(orderId);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ORDER_CANCELLED_KEY,
                    message);
            log.info("[MQ-PUBLISH] order.cancelled → orderId={}", orderId);
        } catch (Exception e) {
            log.error("[MQ-PUBLISH-ERROR] Failed to publish order.cancelled for orderId={}: {}",
                    orderId, e.getMessage(), e);
        }
    }

    // ─── Business Status Events ───────────────────────────────────────────────

    /**
     * Publishes a {@link BusinessStatusMessage} when the admin opens the business.
     */
    public void publishBusinessOpened(UUID adminId, String adminName) {
        BusinessStatusMessage message = new BusinessStatusMessage(adminId, adminName, null);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.BUSINESS_OPENED_KEY,
                    message);
            log.info("[MQ-PUBLISH] business.opened → adminId={}", adminId);
        } catch (Exception e) {
            log.error("[MQ-PUBLISH-ERROR] Failed to publish business.opened: {}",
                    e.getMessage(), e);
        }
    }

    /**
     * Publishes a {@link BusinessStatusMessage} when the admin closes the business.
     *
     * @param closedMessage optional message to display to customers
     */
    public void publishBusinessClosed(UUID adminId, String adminName, String closedMessage) {
        BusinessStatusMessage message = new BusinessStatusMessage(adminId, adminName, closedMessage);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.BUSINESS_CLOSED_KEY,
                    message);
            log.info("[MQ-PUBLISH] business.closed → adminId={}", adminId);
        } catch (Exception e) {
            log.error("[MQ-PUBLISH-ERROR] Failed to publish business.closed: {}",
                    e.getMessage(), e);
        }
    }
}
