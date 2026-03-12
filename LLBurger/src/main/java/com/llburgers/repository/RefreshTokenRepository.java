package com.llburgers.repository;

import com.llburgers.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for refresh token management (rotation, revocation, replay detection).
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /** Find a token by its SHA-256 hash. */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Find all tokens belonging to a family (for replay detection). */
    List<RefreshToken> findByFamilyId(String familyId);

    /** Find all active (non-revoked, non-expired) tokens for a user. */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findActiveTokensByUserId(UUID userId, LocalDateTime now);

    /** Revoke all tokens in a family (replay attack detected). */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.compromised = true WHERE rt.familyId = :familyId")
    int revokeFamily(String familyId);

    /** Revoke all tokens for a user (password change, deactivation, etc.). */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    int revokeAllForUser(UUID userId);

    /** Delete expired tokens (housekeeping). */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    int deleteExpiredTokens(LocalDateTime cutoff);
}
