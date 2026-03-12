package com.llburgers.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a refresh token stored in the database for rotation and revocation.
 *
 * <p>Each token belongs to a "family" — if an old token is reused (replay attack),
 * the entire family is invalidated, forcing the user to re-authenticate.</p>
 *
 * <ul>
 *   <li>On every refresh, the current token is marked {@code revoked} and a new one is issued.</li>
 *   <li>On password change, role change, or account deactivation, all tokens for the user are revoked.</li>
 *   <li>The {@code tokenHash} stores a SHA-256 hash — the raw token is never persisted.</li>
 * </ul>
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_rt_token_hash", columnList = "tokenHash"),
    @Index(name = "idx_rt_user_id",    columnList = "user_id"),
    @Index(name = "idx_rt_family",     columnList = "familyId")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * SHA-256 hash of the actual JWT refresh token.
     * The raw token is only sent to the client; we never store it in plain text.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** Unique identifier for this token family (set on initial login, inherited on rotation). */
    @Column(nullable = false, length = 36)
    private String familyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** True if this token has been used (rotated) or explicitly revoked. */
    @Column(nullable = false)
    private boolean revoked = false;

    /** True if replay was detected (an old token from this family was reused). */
    @Column(nullable = false)
    private boolean compromised = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public UUID getId()                          { return id; }

    public String getTokenHash()                 { return tokenHash; }
    public void   setTokenHash(String h)         { this.tokenHash = h; }

    public String getFamilyId()                  { return familyId; }
    public void   setFamilyId(String f)          { this.familyId = f; }

    public User getUser()                        { return user; }
    public void setUser(User u)                  { this.user = u; }

    public LocalDateTime getExpiresAt()          { return expiresAt; }
    public void setExpiresAt(LocalDateTime t)    { this.expiresAt = t; }

    public boolean isRevoked()                   { return revoked; }
    public void    setRevoked(boolean r)         { this.revoked = r; }

    public boolean isCompromised()               { return compromised; }
    public void    setCompromised(boolean c)     { this.compromised = c; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
}
