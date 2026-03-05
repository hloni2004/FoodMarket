package com.llburgers.service;

import com.llburgers.domain.Customer;
import com.llburgers.domain.Order;

/**
 * Multi-provider email service.
 * <ul>
 *     <li><b>Brevo</b> — Welcome emails, business open/close notifications</li>
 *     <li><b>Mailjet</b> — Order confirmations, order status updates</li>
 * </ul>
 * Emails are sent asynchronously with automatic retry on failure.
 */
public interface IEmailService {

    // ─── Brevo (Sender 1) — Welcome & Business ───────────────────────────────

    void sendWelcomeEmail(Customer customer);

    void sendBusinessOpenedEmail(Customer customer);

    void sendBusinessClosedEmail(Customer customer, String closedMessage);

    // ─── Mailjet (Sender 2) — Order Emails ────────────────────────────────────

    void sendOrderConfirmationEmail(Order order);

    void sendOrderStatusUpdateEmail(Order order);
}

