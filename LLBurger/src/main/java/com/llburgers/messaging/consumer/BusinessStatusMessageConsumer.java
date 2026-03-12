package com.llburgers.messaging.consumer;

import com.llburgers.config.RabbitMQConfig;
import com.llburgers.domain.Customer;
import com.llburgers.domain.Notification;
import com.llburgers.domain.enums.NotificationStatus;
import com.llburgers.domain.enums.NotificationType;
import com.llburgers.messaging.BusinessStatusMessage;
import com.llburgers.repository.CustomerRepository;
import com.llburgers.repository.NotificationRepository;
import com.llburgers.service.IEmailService;
import com.llburgers.websocket.WebSocketPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RabbitMQ consumer that processes business-status messages and
 * broadcasts emails to all active customers.
 *
 * <p><b>Queues consumed:</b></p>
 * <ul>
 *     <li>{@code llburger.business.opened} → "business opened" email (Brevo) to all active customers</li>
 *     <li>{@code llburger.business.closed} → "business closed" email (Brevo) to all active customers</li>
 * </ul>
 *
 * <p>Each customer email is attempted independently; a single failure does not
 * abort the broadcast. The overall notification is counted in the logs.</p>
 */
@Component
public class BusinessStatusMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(BusinessStatusMessageConsumer.class);

    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final IEmailService emailService;
    private final WebSocketPushService wsPushService;

    public BusinessStatusMessageConsumer(CustomerRepository customerRepository,
                                         NotificationRepository notificationRepository,
                                         IEmailService emailService,
                                         WebSocketPushService wsPushService) {
        this.customerRepository = customerRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.wsPushService = wsPushService;
    }

    // ─── Business Opened ──────────────────────────────────────────────────────

    /**
     * Sends a "business is now open" email (Brevo) to every active customer
     * and creates a notification record per customer.
     */
    @RabbitListener(queues = RabbitMQConfig.BUSINESS_OPENED_QUEUE)
    @Transactional
    public void handleBusinessOpened(BusinessStatusMessage message) {
        log.info("[MQ-CONSUME] business.opened → adminId={}", message.adminId());

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
                log.error("[MQ-CONSUME] Business-opened email FAILED for customer={}: {}",
                        customer.getEmail(), e.getMessage());
            }
            notificationRepository.save(notification);
        }

        log.info("[MQ-CONSUME] business.opened broadcast complete – sent={}, failed={}, adminId={}",
                sent, failed, message.adminId());

        // Real-time WebSocket broadcast to all connected clients
        if (!activeCustomers.isEmpty()) {
            Notification broadcastNotification = notificationRepository.save(Notification.builder()
                    .type(NotificationType.BUSINESS_OPENED)
                    .status(NotificationStatus.SENT)
                    .build());
            wsPushService.pushBusinessEvent(broadcastNotification, null);
        }
    }

    // ─── Business Closed ──────────────────────────────────────────────────────

    /**
     * Sends a "business is now closed" email (Brevo) to every active customer
     * and creates a notification record per customer.
     */
    @RabbitListener(queues = RabbitMQConfig.BUSINESS_CLOSED_QUEUE)
    @Transactional
    public void handleBusinessClosed(BusinessStatusMessage message) {
        log.info("[MQ-CONSUME] business.closed → adminId={}", message.adminId());

        List<Customer> activeCustomers = customerRepository.findByActive(true);
        String closedMessage = message.closedMessage();
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
                log.error("[MQ-CONSUME] Business-closed email FAILED for customer={}: {}",
                        customer.getEmail(), e.getMessage());
            }
            notificationRepository.save(notification);
        }

        log.info("[MQ-CONSUME] business.closed broadcast complete – sent={}, failed={}, adminId={}",
                sent, failed, message.adminId());

        // Real-time WebSocket broadcast to all connected clients
        if (!activeCustomers.isEmpty()) {
            Notification broadcastNotification = notificationRepository.save(Notification.builder()
                    .type(NotificationType.BUSINESS_CLOSED)
                    .status(NotificationStatus.SENT)
                    .build());
            wsPushService.pushBusinessEvent(broadcastNotification, closedMessage);
        }
    }
}
