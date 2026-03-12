package com.llburgers.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Single-use token emailed to a user so they can reset their password.
 *
 * <ul>
 *   <li>Expires 1 hour after creation.</li>
 *   <li>Marked {@code used = true} immediately after the password is changed.</li>
 *   <li>Any expired or used token is rejected by {@link com.llburgers.controller.AuthController}.</li>
 * </ul>
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_prt_token",   columnList = "token"),
    @Index(name = "idx_prt_user_id", columnList = "user_id")
})
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** 64-char hex token generated from two random UUIDs. */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

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

    public UUID getId()                        { return id; }

    public String getToken()                   { return token; }
    public void   setToken(String token)       { this.token = token; }

    public User getUser()                      { return user; }
    public void setUser(User user)             { this.user = user; }

    public LocalDateTime getExpiresAt()        { return expiresAt; }
    public void setExpiresAt(LocalDateTime t)  { this.expiresAt = t; }

    public boolean isUsed()                    { return used; }
    public void    setUsed(boolean used)       { this.used = used; }

    public LocalDateTime getCreatedAt()        { return createdAt; }
}
