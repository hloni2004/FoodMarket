package com.llburgers.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks failed login attempts per email address using Redis with Caffeine fallback.
 *
 * <p>After {@code security.login.max-attempts} consecutive failures the
 * account is temporarily locked for {@code security.login.lock-duration-minutes}
 * minutes.</p>
 *
 * <h2>Fallback Strategy</h2>
 * <ul>
 *   <li>Primary: Redis (distributed, survives restarts)</li>
 *   <li>Fallback: Caffeine in-memory cache (single-node, but better than nothing)</li>
 *   <li>Admin/Super accounts: Fail-closed (locked if Redis unavailable)</li>
 *   <li>Customer accounts: Fail-open (allowed if both caches fail)</li>
 * </ul>
 */
@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private static final String ATTEMPTS_PREFIX = "login_attempts:";
    private static final String LOCKED_PREFIX   = "login_locked:";

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.lock-duration-minutes:15}")
    private long lockDurationMinutes;

    private final StringRedisTemplate redis;

    /** In-memory fallback cache for attempts (email -> count). */
    private final Cache<String, AtomicLong> localAttempts;

    /** In-memory fallback cache for locks (email -> locked until timestamp). */
    private final Cache<String, Long> localLocks;

    /** Emails of admin/super users who should fail-closed. */
    private final Set<String> adminEmails;

    public LoginAttemptService(StringRedisTemplate redis) {
        this.redis = redis;
        this.adminEmails = Set.of(); // Could be populated from DB or config

        // Configure Caffeine caches with same TTL as Redis
        this.localAttempts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(15))
                .maximumSize(10_000)
                .build();

        this.localLocks = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(15))
                .maximumSize(10_000)
                .build();
    }

    /**
     * Returns {@code true} if the account is currently locked.
     *
     * @param email     The email to check
     * @param isAdmin   Whether this is an admin/super account (fail-closed)
     */
    public boolean isLocked(String email, boolean isAdmin) {
        String normalized = normalize(email);

        // Try Redis first
        try {
            Boolean locked = redis.hasKey(LOCKED_PREFIX + normalized);
            if (Boolean.TRUE.equals(locked)) {
                return true;
            }
        } catch (Exception e) {
            log.warn("[RATE-LIMIT] Redis unavailable for lockout check: {}", e.getMessage());

            // Admin accounts: fail-closed (assume locked if Redis is down)
            if (isAdmin) {
                log.warn("[RATE-LIMIT] Fail-closed: blocking admin {} due to Redis unavailability", email);
                return true;
            }

            // Fall back to local cache for customers
            Long lockedUntil = localLocks.getIfPresent(normalized);
            if (lockedUntil != null && lockedUntil > System.currentTimeMillis()) {
                return true;
            }
        }

        return false;
    }

    /** Convenience overload for non-admin checks. */
    public boolean isLocked(String email) {
        return isLocked(email, false);
    }

    /** Clears all failure counters after a successful login. */
    public void recordSuccess(String email) {
        String normalized = normalize(email);

        // Clear Redis
        try {
            redis.delete(ATTEMPTS_PREFIX + normalized);
            redis.delete(LOCKED_PREFIX + normalized);
        } catch (Exception e) {
            log.warn("[RATE-LIMIT] Redis unavailable – could not clear counters for {}", email);
        }

        // Clear local cache
        localAttempts.invalidate(normalized);
        localLocks.invalidate(normalized);
    }

    /**
     * Increments the failure counter. Locks the account once
     * {@code maxAttempts} is reached.
     */
    public void recordFailure(String email) {
        String normalized = normalize(email);
        long attempts = 0;

        // Try Redis first
        try {
            String key = ATTEMPTS_PREFIX + normalized;
            Long redisAttempts = redis.opsForValue().increment(key);
            redis.expire(key, Duration.ofMinutes(lockDurationMinutes));
            attempts = redisAttempts != null ? redisAttempts : 0;

            if (attempts >= maxAttempts) {
                String lockKey = LOCKED_PREFIX + normalized;
                redis.opsForValue().set(lockKey, "1");
                redis.expire(lockKey, Duration.ofMinutes(lockDurationMinutes));
                log.warn("[LOGIN-LOCKED] Account locked after {} failed attempts: {}", attempts, email);
            }
        } catch (Exception e) {
            log.warn("[RATE-LIMIT] Redis unavailable – using local cache for {}", email);

            // Fall back to local cache
            AtomicLong counter = localAttempts.get(normalized, k -> new AtomicLong(0));
            attempts = counter.incrementAndGet();

            if (attempts >= maxAttempts) {
                long lockedUntil = System.currentTimeMillis() + (lockDurationMinutes * 60 * 1000);
                localLocks.put(normalized, lockedUntil);
                log.warn("[LOGIN-LOCKED] Account locked (local cache) after {} failed attempts: {}", attempts, email);
            }
        }
    }

    /** Returns how many more attempts the user has before lockout. */
    public long getRemainingAttempts(String email) {
        String normalized = normalize(email);

        // Try Redis first
        try {
            String val = redis.opsForValue().get(ATTEMPTS_PREFIX + normalized);
            long used = val != null ? Long.parseLong(val) : 0L;
            return Math.max(0, maxAttempts - used);
        } catch (Exception e) {
            // Fall back to local cache
            AtomicLong counter = localAttempts.getIfPresent(normalized);
            long used = counter != null ? counter.get() : 0L;
            return Math.max(0, maxAttempts - used);
        }
    }

    private String normalize(String email) {
        return email == null ? "" : email.toLowerCase().trim();
    }
}
