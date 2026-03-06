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
    
    @Value("${mail.sender1.from}")
    private String brevoFromAddress;

    public EmailTestController(@Qualifier("brevoMailSender") JavaMailSender brevoSender) {
        this.brevoSender = brevoSender;
    }

    @GetMapping("/email")
    public ResponseEntity<Map<String, String>> testEmail(@RequestParam String to) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(brevoFromAddress);
            message.setTo(to);
            message.setSubject("Test Email from LLBurger");
            message.setText("This is a test email. If you see this, SMTP is working correctly.");
            
            brevoSender.send(message);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Email sent successfully to " + to,
                    "from", brevoFromAddress
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "cause", e.getCause() != null ? e.getCause().toString() : "none",
                    "from", brevoFromAddress
            ));
        }
    }
}
