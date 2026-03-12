package com.llburgers.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One-time password (OTP) token used for the forgot-password flow.
 *
 * <p>A 6-digit numeric code is generated, stored here (hashed), and emailed
 * to the user. The user must submit it within 10 minutes. On successful
 * verification a {@link PasswordResetToken} is issued for the actual reset.</p>
 */
@Entity
@Table(name = "otp_tokens", indexes = {
    @Index(name = "idx_otp_email", columnList = "email")
})
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The email address this OTP was issued for. */
    @Column(nullable = false, length = 255)
    private String email;

    /** BCrypt hash of the 6-digit code (never store plain OTP). */
    @Column(nullable = false)
    private String otpHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public UUID getId()                          { return id; }

    public String getEmail()                     { return email; }
    public void   setEmail(String email)         { this.email = email; }

    public String getOtpHash()                   { return otpHash; }
    public void   setOtpHash(String otpHash)     { this.otpHash = otpHash; }

    public LocalDateTime getExpiresAt()          { return expiresAt; }
    public void setExpiresAt(LocalDateTime t)    { this.expiresAt = t; }

    public boolean isUsed()                      { return used; }
    public void    setUsed(boolean used)         { this.used = used; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
}
