package com.llburgers.service.impl;

import com.llburgers.domain.Customer;
import com.llburgers.domain.Order;
import com.llburgers.domain.OrderItem;
import com.llburgers.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Multi-provider email service implementation.
 * <ul>
 *     <li><b>Brevo</b>   — welcome emails, business open/close notifications</li>
 *     <li><b>Mailjet</b> — order confirmations, order status updates</li>
 * </ul>
 * Every send is asynchronous and includes a retry mechanism with logging.
 */
@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final int MAX_RETRIES = 3;

    private final JavaMailSender brevoSender;
    private final JavaMailSender mailjetSender;

    @Value("${mail.sender1.from}")
    private String brevoFromAddress;

    @Value("${spring.mail.from}")
    private String mailjetFromAddress;

    public EmailServiceImpl(@Qualifier("brevoMailSender") JavaMailSender brevoSender,
                            @Qualifier("mailjetMailSender") JavaMailSender mailjetSender) {
        this.brevoSender = brevoSender;
        this.mailjetSender = mailjetSender;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BREVO (Sender 1) — Welcome & Business Notifications
    // ═══════════════════════════════════════════════════════════════════════════

    @Async
    @Override
    public void sendWelcomeEmail(Customer customer) {
        log.debug("[ASYNC-START] sendWelcomeEmail for {}", customer.getEmail());
        String subject = "Welcome to LL Burgers! 🍔";
        String body = buildWelcomeEmailBody(customer);
        sendWithRetry(brevoSender, brevoFromAddress, customer.getEmail(), subject, body, "WELCOME");
        log.debug("[ASYNC-END] sendWelcomeEmail for {}", customer.getEmail());
    }

    @Async
    @Override
    public void sendBusinessOpenedEmail(Customer customer) {
        String subject = "LL Burgers is Now Open! 🟢";
        String body = buildBusinessOpenedBody(customer);
        sendWithRetry(brevoSender, brevoFromAddress, customer.getEmail(), subject, body, "BUSINESS_OPENED");
    }

    @Async
    @Override
    public void sendBusinessClosedEmail(Customer customer, String closedMessage) {
        String subject = "LL Burgers is Now Closed 🔴";
        String body = buildBusinessClosedBody(customer, closedMessage);
        sendWithRetry(brevoSender, brevoFromAddress, customer.getEmail(), subject, body, "BUSINESS_CLOSED");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAILJET (Sender 2) — Order Emails
    // ═══════════════════════════════════════════════════════════════════════════

    @Async
    @Override
    public void sendOrderConfirmationEmail(Order order) {
        String to = order.getCustomer().getEmail();
        String subject = "Order Confirmed — #" + order.getId().toString().substring(0, 8);
        String body = buildOrderConfirmationBody(order);
        sendWithRetry(mailjetSender, mailjetFromAddress, to, subject, body, "ORDER_CONFIRMATION");
    }

    @Async
    @Override
    public void sendOrderStatusUpdateEmail(Order order) {
        String to = order.getCustomer().getEmail();
        String subject = "Order Update — " + order.getStatus().name();
        String body = buildOrderStatusUpdateBody(order);
        sendWithRetry(mailjetSender, mailjetFromAddress, to, subject, body, "ORDER_STATUS_UPDATE");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RETRY MECHANISM
    // ═══════════════════════════════════════════════════════════════════════════

    private void sendWithRetry(JavaMailSender sender, String from, String to,
                               String subject, String body, String emailType) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                sender.send(message);

                log.info("[EMAIL-SENT] type={}, to={}, attempt={}", emailType, to, attempt);
                return;
            } catch (MailException e) {
                log.warn("[EMAIL-RETRY] type={}, to={}, attempt={}/{}, error={}",
                        emailType, to, attempt, MAX_RETRIES, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    log.error("[EMAIL-FAILED] type={}, to={}, exhausted all {} retries. Last error: {}",
                            emailType, to, MAX_RETRIES, e.getMessage());
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EMAIL BODY BUILDERS
    // ═══════════════════════════════════════════════════════════════════════════

    private String buildWelcomeEmailBody(Customer customer) {
        return """
                Welcome to LL Burgers, %s! 🍔
                
                Thank you for joining us. Your account has been created successfully.
                
                Delivery Details:
                  - Block: %s
                  - Room: %s
                
                Browse our menu and place your first order today!
                
                LL Burgers — Delivered to your door.
                """.formatted(customer.getName(), customer.getBlock(), customer.getRoomNumber());
    }

    private String buildBusinessOpenedBody(Customer customer) {
        return """
                LL Burgers is Now Open! 🟢
                
                Hey %s, we're open for orders!
                Head over and grab your favourite burger now.
                
                LL Burgers — Delivered to your door.
                """.formatted(customer.getName());
    }

    private String buildBusinessClosedBody(Customer customer, String closedMessage) {
        String note = (closedMessage != null && !closedMessage.isBlank())
                ? "\nNote: " + closedMessage + "\n"
                : "";
        return """
                LL Burgers is Now Closed 🔴
                
                Hey %s, we've closed for now.
                %s
                We'll notify you as soon as we reopen!
                
                LL Burgers — Delivered to your door.
                """.formatted(customer.getName(), note);
    }

    private String buildOrderConfirmationBody(Order order) {
        StringBuilder items = new StringBuilder();
        for (OrderItem item : order.getOrderItems()) {
            items.append("  - ")
                    .append(item.getProduct().getName())
                    .append(" x").append(item.getQuantity())
                    .append(" — R").append(item.getTotalPrice())
                    .append("\n");
        }

        String specialNote = (order.getSpecialInstructions() != null && !order.getSpecialInstructions().isBlank())
                ? "\nSpecial Instructions / Allergies: " + order.getSpecialInstructions() + "\n"
                : "";

        return """
                Order Confirmed ✅
                
                Hi %s, your order has been placed successfully!
                
                Order ID: %s
                Delivery: Block %s, Room %s
                %s
                Items:
                %s
                Total: R%s
                
                LL Burgers — Delivered to your door.
                """.formatted(
                order.getCustomer().getName(),
                order.getId().toString().substring(0, 8),
                order.getDeliveryBlock(),
                order.getDeliveryRoomNumber(),
                specialNote,
                items.toString(),
                order.getTotalPrice()
        );
    }

    private String buildOrderStatusUpdateBody(Order order) {
        String statusEmoji = switch (order.getStatus()) {
            case PROCESSING -> "🔄";
            case ON_THE_WAY -> "🚗";
            case DELIVERED -> "✅";
        };

        return """
                Order Update %s
                
                Hi %s, your order #%s status has changed:
                
                Status: %s
                
                Delivery: Block %s, Room %s
                
                LL Burgers — Delivered to your door.
                """.formatted(
                statusEmoji,
                order.getCustomer().getName(),
                order.getId().toString().substring(0, 8),
                order.getStatus().name().replace("_", " "),
                order.getDeliveryBlock(),
                order.getDeliveryRoomNumber()
        );
    }
}



