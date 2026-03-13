package com.llburgers.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Test endpoint to verify SMTP connectivity.
 * DELETE THIS FILE after debugging is complete.
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin
public class EmailTestController {

    private final JavaMailSender brevoSender;
    private final JavaMailSender mailjetSender;
    
    @Value("${mail.sender1.from}")
    private String brevoFromAddress;

    @Value("${spring.mail.from}")
    private String mailjetFromAddress;

    public EmailTestController(@Qualifier("brevoMailSender") JavaMailSender brevoSender,
                               @Qualifier("mailjetMailSender") JavaMailSender mailjetSender) {
        this.brevoSender = brevoSender;
        this.mailjetSender = mailjetSender;
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, String>> testEmail(@RequestParam String to,
                                                         @RequestParam(defaultValue = "brevo") String provider) {
        try {
            boolean useMailjet = "mailjet".equalsIgnoreCase(provider);
            JavaMailSender selectedSender = useMailjet ? mailjetSender : brevoSender;
            String fromAddress = useMailjet ? mailjetFromAddress : brevoFromAddress;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Test Email from LLBurger (" + (useMailjet ? "MAILJET" : "BREVO") + ")");
            message.setText("This is a test email. If you see this, SMTP is working correctly.");
            
            selectedSender.send(message);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Email sent successfully to " + to,
                    "provider", useMailjet ? "mailjet" : "brevo",
                    "from", fromAddress
            ));
        } catch (Exception e) {
            boolean useMailjet = "mailjet".equalsIgnoreCase(provider);
            String fromAddress = useMailjet ? mailjetFromAddress : brevoFromAddress;
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "cause", e.getCause() != null ? e.getCause().toString() : "none",
                    "provider", useMailjet ? "mailjet" : "brevo",
                    "from", fromAddress
            ));
        }
    }
}
