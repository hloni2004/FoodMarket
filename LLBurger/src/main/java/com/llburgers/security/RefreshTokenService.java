package com.llburgers.security;

import com.llburgers.domain.RefreshToken;
import com.llburgers.domain.User;
import com.llburgers.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages refresh token lifecycle: creation, rotation, validation, and revocation.
 *
 * <h2>Token Rotation</h2>
 * <p>Every time a refresh token is used, it is marked as revoked and a new token
 * is issued within the same "family". If a revoked token is replayed, the entire
 * family is invalidated (theft detection).</p>
 *
 * <h2>Revocation Triggers</h2>
 * <ul>
 *   <li>Password change / reset</li>
 *   <li>Role change</li>
 *   <li>Account deactivation</li>
 *   <li>Manual logout (single token)</li>
 *   <li>Replay attack detected (entire family)</li>
 * </ul>
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    // ─── Token creation ───────────────────────────────────────────────────────

    /**
     * Creates a new refresh token for the user and persists it.
     * Called on initial login/registration.
     *
     * @return The raw JWT refresh token (to be sent to the client).
     */
    @Transactional
    public String createRefreshToken(User user) {
        String familyId = UUID.randomUUID().toString();
        return createTokenInFamily(user, familyId);
    }

    /**
     * Rotates a refresh token: validates the old one, revokes it, and issues a new one.
     *
     * @param rawOldToken The raw JWT token from the client.
     * @return The new raw JWT token, or empty if rotation failed (invalid/revoked/expired).
     */
    @Transactional
    public Optional<String> rotateRefreshToken(String rawOldToken) {
        String hash = hashToken(rawOldToken);
        Optional<RefreshToken> existing = refreshTokenRepository.findByTokenHash(hash);

        if (existing.isEmpty()) {
            // Token not found — could be already deleted or never existed
            log.warn("[REFRESH-TOKEN] Unknown token hash — rejecting");
            return Optional.empty();
        }

        RefreshToken old = existing.get();

        // Check if already revoked (replay attack!)
        if (old.isRevoked()) {
            log.warn("[REFRESH-TOKEN] REPLAY DETECTED for family {} — revoking entire family", old.getFamilyId());
            refreshTokenRepository.revokeFamily(old.getFamilyId());
            return Optional.empty();
        }

        // Check expiry
        if (old.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.debug("[REFRESH-TOKEN] Expired token — rejecting");
            old.setRevoked(true);
            refreshTokenRepository.save(old);
            return Optional.empty();
        }

        // Check if user is still active
        User user = old.getUser();
        if (!user.isActive()) {
            log.info("[REFRESH-TOKEN] User {} is inactive — revoking all tokens", user.getEmail());
            refreshTokenRepository.revokeAllForUser(user.getId());
            return Optional.empty();
        }

        // Mark old token as revoked (used)
        old.setRevoked(true);
        refreshTokenRepository.save(old);

        // Issue new token in the same family
        String newToken = createTokenInFamily(user, old.getFamilyId());
        log.debug("[REFRESH-TOKEN] Rotated token for user {} in family {}", user.getEmail(), old.getFamilyId());

        return Optional.of(newToken);
    }

    /**
     * Validates a refresh token without rotation (e.g., for /me endpoint pre-check).
     */
    public Optional<User> validateRefreshToken(String rawToken) {
        String hash = hashToken(rawToken);
        return refreshTokenRepository.findByTokenHash(hash)
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(RefreshToken::getUser)
                .filter(User::isActive);
    }

    // ─── Revocation ───────────────────────────────────────────────────────────

    /**
     * Revokes a single refresh token (e.g., on logout).
     */
    @Transactional
    public void revokeToken(String rawToken) {
        String hash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.debug("[REFRESH-TOKEN] Revoked single token for user {}", rt.getUser().getEmail());
        });
    }

    /**
     * Revokes ALL refresh tokens for a user.
     * Call this on: password change, role change, account deactivation.
     */
    @Transactional
    public void revokeAllForUser(UUID userId) {
        int count = refreshTokenRepository.revokeAllForUser(userId);
        log.info("[REFRESH-TOKEN] Revoked {} tokens for user {}", count, userId);
    }

    // ─── Housekeeping ─────────────────────────────────────────────────────────

    /**
     * Periodically delete expired tokens to keep the table clean.
     * Runs every 6 hours.
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("[REFRESH-TOKEN] Cleaned up {} expired tokens", deleted);
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private String createTokenInFamily(User user, String familyId) {
        String rawToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());
        String hash = hashToken(rawToken);

        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(hash);
        entity.setFamilyId(familyId);
        entity.setUser(user);
        entity.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtService.getRefreshTokenExpirationMs())));
        entity.setRevoked(false);
        entity.setCompromised(false);

        refreshTokenRepository.save(entity);
        return rawToken;
    }

    /**
     * SHA-256 hash of the token for storage. Never store the raw token.
     */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
