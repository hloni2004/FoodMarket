package com.llburgers.service.impl;

import com.llburgers.domain.Customer;
import com.llburgers.domain.Order;
import com.llburgers.domain.OrderItem;
import com.llburgers.service.IEmailService;
import com.llburgers.util.EmailTemplateBuilder;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
        String subject = "Welcome to LL Burgers";
        String htmlBody = EmailTemplateBuilder.welcome(
                customer.getName(),
                String.valueOf(customer.getBlock()),
                customer.getRoomNumber()
        );
        sendWithRetry(brevoSender, brevoFromAddress, customer.getEmail(), subject, htmlBody, "WELCOME");
        log.debug("[ASYNC-END] sendWelcomeEmail for {}", customer.getEmail());
    }

    @Async
    @Override
    public void sendBusinessOpenedEmail(Customer customer) {
        String subject = "LL Burgers is now open";
        String htmlBody = EmailTemplateBuilder.businessOpened(customer.getName());
        sendWithRetry(brevoSender, brevoFromAddress, customer.getEmail(), subject, htmlBody, "BUSINESS_OPENED");
    }

    @Async
    @Override
    public void sendBusinessClosedEmail(Customer customer, String closedMessage) {
        String subject = "LL Burgers is now closed";
        String htmlBody = EmailTemplateBuilder.businessClosed(customer.getName(), closedMessage);
        sendWithRetry(brevoSender, brevoFromAddress, customer.getEmail(), subject, htmlBody, "BUSINESS_CLOSED");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAILJET (Sender 2) — Order Emails
    // ═══════════════════════════════════════════════════════════════════════════

    @Async
    @Override
    public void sendOrderConfirmationEmail(Order order) {
        String to = order.getCustomer().getEmail();
        String subject = "Order Confirmed — #" + order.getId().toString().substring(0, 8);
        String htmlBody = buildOrderConfirmationBody(order);
        sendOrderEmailWithFallback(to, subject, htmlBody, "ORDER_CONFIRMATION");
    }

    @Async
    @Override
    public void sendOrderStatusUpdateEmail(Order order) {
        String to = order.getCustomer().getEmail();
        String subject = "Order Update — " + order.getStatus().name();
        String htmlBody = buildOrderStatusUpdateBody(order);
        sendOrderEmailWithFallback(to, subject, htmlBody, "ORDER_STATUS_UPDATE");
    }

    private void sendOrderEmailWithFallback(String to, String subject, String htmlBody, String emailType) {
        boolean sentViaMailjet = sendWithRetry(
                mailjetSender,
                mailjetFromAddress,
                to,
                subject,
                htmlBody,
                emailType + "_MAILJET"
        );

        if (sentViaMailjet) {
            return;
        }

        log.warn("[EMAIL-FALLBACK] type={}, to={}, provider=BREVO", emailType, to);
        sendWithRetry(
                brevoSender,
                brevoFromAddress,
                to,
                subject,
            htmlBody,
                emailType + "_BREVO_FALLBACK"
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RETRY MECHANISM
    // ═══════════════════════════════════════════════════════════════════════════

    private boolean sendWithRetry(JavaMailSender sender, String from, String to,
                                  String subject, String htmlBody, String emailType) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                var message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
                helper.setFrom(from);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
                sender.send(message);

                log.info("[EMAIL-SENT] type={}, to={}, attempt={}", emailType, to, attempt);
                return true;
            } catch (MailException | MessagingException e) {
                log.warn("[EMAIL-RETRY] type={}, to={}, attempt={}/{}, error={}",
                        emailType, to, attempt, MAX_RETRIES, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    log.error("[EMAIL-FAILED] type={}, to={}, exhausted all {} retries. Last error: {}",
                            emailType, to, MAX_RETRIES, e.getMessage());
                }
            }
        }

        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EMAIL BODY BUILDERS
    // ═══════════════════════════════════════════════════════════════════════════

    private String buildWelcomeEmailBody(Customer customer) {
        return EmailTemplateBuilder.welcome(
            customer.getName(),
            String.valueOf(customer.getBlock()),
            customer.getRoomNumber()
        );
    }

    private String buildBusinessOpenedBody(Customer customer) {
        return EmailTemplateBuilder.businessOpened(customer.getName());
    }

    private String buildBusinessClosedBody(Customer customer, String closedMessage) {
        return EmailTemplateBuilder.businessClosed(customer.getName(), closedMessage);
    }

    private String buildOrderConfirmationBody(Order order) {
        StringBuilder itemRows = new StringBuilder();
        for (OrderItem item : order.getOrderItems()) {
            itemRows.append(EmailTemplateBuilder.orderItemRow(
                item.getProduct().getName(),
                item.getQuantity(),
                "R" + item.getTotalPrice()
            ));
        }

        return EmailTemplateBuilder.orderConfirmation(
            order.getCustomer().getName(),
            order.getId().toString().substring(0, 8),
            String.valueOf(order.getDeliveryBlock()),
            order.getDeliveryRoomNumber(),
            EmailTemplateBuilder.orderItemsTable(itemRows.toString()),
            "R" + order.getTotalPrice(),
            order.getSpecialInstructions()
        );
    }

    private String buildOrderStatusUpdateBody(Order order) {
        String statusEmoji = switch (order.getStatus()) {
            case PROCESSING -> "🔄";
            case ON_THE_WAY -> "🚗";
            case DELIVERED -> "✅";
        };

        return EmailTemplateBuilder.orderStatusUpdate(
                order.getCustomer().getName(),
                order.getId().toString().substring(0, 8),
            statusEmoji + " " + order.getStatus().name().replace("_", " "),
            String.valueOf(order.getDeliveryBlock()),
                order.getDeliveryRoomNumber()
        );
    }
}



