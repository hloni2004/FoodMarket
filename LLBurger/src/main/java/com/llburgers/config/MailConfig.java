package com.llburgers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configures two email providers as per the multi-provider strategy:
 * <ul>
 *     <li><b>Sender 1 (Brevo)</b> — Welcome emails and business-status notifications</li>
 *     <li><b>Sender 2 (Mailjet)</b> — Order confirmations and status-update emails</li>
 * </ul>
 */
@Configuration
public class MailConfig {

    // ─── Sender 1: Brevo (welcome + business notifications) ───────────────────

    @Value("${mail.sender1.host}")
    private String sender1Host;

    @Value("${mail.sender1.port}")
    private int sender1Port;

    @Value("${mail.sender1.username}")
    private String sender1Username;

    @Value("${mail.sender1.password}")
    private String sender1Password;

    @Value("${mail.sender1.properties.mail.smtp.auth}")
    private boolean sender1Auth;

    @Value("${mail.sender1.properties.mail.smtp.starttls.enable}")
    private boolean sender1Starttls;

    // ─── Sender 2: Mailjet (order emails) ─────────────────────────────────────

    @Value("${spring.mail.host}")
    private String sender2Host;

    @Value("${spring.mail.port}")
    private int sender2Port;

    @Value("${spring.mail.username}")
    private String sender2Username;

    @Value("${spring.mail.password}")
    private String sender2Password;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean sender2Auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean sender2Starttls;

    // ─── Bean Definitions ─────────────────────────────────────────────────────

    /**
     * Brevo — used for welcome emails and business open/close notifications.
     */
    @Bean(name = "brevoMailSender")
    public JavaMailSender brevoMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(sender1Host);
        mailSender.setPort(sender1Port);
        mailSender.setUsername(sender1Username);
        mailSender.setPassword(sender1Password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(sender1Auth));
        props.put("mail.smtp.starttls.enable", String.valueOf(sender1Starttls));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }

    /**
     * Mailjet — used for order confirmation and status-update emails.
     */
    @Primary
    @Bean(name = "mailjetMailSender")
    public JavaMailSender mailjetMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(sender2Host);
        mailSender.setPort(sender2Port);
        mailSender.setUsername(sender2Username);
        mailSender.setPassword(sender2Password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(sender2Auth));
        props.put("mail.smtp.starttls.enable", String.valueOf(sender2Starttls));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }
}

