package com.llburgers.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limits the forgot-password endpoint to prevent email flooding/abuse.
 *
 * <p>Limits requests per email address and per IP address to prevent
 * both targeted email bombing and distributed abuse.</p>
 *
 * <h2>Limits (configurable)</h2>
 * <ul>
 *   <li>Per email: 3 requests per hour</li>
 *   <li>Per IP: 10 requests per hour</li>
 * </ul>
 */
@Service
public class PasswordResetRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetRateLimiter.class);

    private static final String EMAIL_PREFIX = "pwd_reset_email:";
    private static final String IP_PREFIX    = "pwd_reset_ip:";

    @Value("${security.password-reset.max-per-email:3}")
    private int maxPerEmail;

    @Value("${security.password-reset.max-per-ip:10}")
    private int maxPerIp;

    @Value("${security.password-reset.window-minutes:60}")
    private long windowMinutes;

    private final StringRedisTemplate redis;

    /** Local fallback for email rate limiting. */
    private final Cache<String, AtomicLong> localEmailLimits;

    /** Local fallback for IP rate limiting. */
    private final Cache<String, AtomicLong> localIpLimits;

    public PasswordResetRateLimiter(StringRedisTemplate redis) {
        this.redis = redis;

        this.localEmailLimits = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(60))
                .maximumSize(10_000)
                .build();

        this.localIpLimits = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(60))
                .maximumSize(10_000)
                .build();
    }

    /**
     * Checks if a password reset request is allowed.
     *
     * @param email The email requesting reset
     * @param ip    The client IP address
     * @return null if allowed, or an error message if rate limited
     */
    public String checkRateLimit(String email, String ip) {
        String normalizedEmail = normalize(email);
        String normalizedIp = normalize(ip);

        // Check email limit
        long emailCount = getAndIncrement(EMAIL_PREFIX, normalizedEmail, localEmailLimits);
        if (emailCount > maxPerEmail) {
            log.warn("[PWD-RESET-RATE] Email limit exceeded: {}", email);
            return "Too many password reset requests for this email. Please try again later.";
        }

        // Check IP limit
        long ipCount = getAndIncrement(IP_PREFIX, normalizedIp, localIpLimits);
        if (ipCount > maxPerIp) {
            log.warn("[PWD-RESET-RATE] IP limit exceeded: {}", ip);
            return "Too many password reset requests from this location. Please try again later.";
        }

        return null; // Allowed
    }

    private long getAndIncrement(String prefix, String key, Cache<String, AtomicLong> fallbackCache) {
        // Try Redis first
        try {
            String redisKey = prefix + key;
            Long count = redis.opsForValue().increment(redisKey);
            redis.expire(redisKey, Duration.ofMinutes(windowMinutes));
            return count != null ? count : 1;
        } catch (Exception e) {
            log.debug("[PWD-RESET-RATE] Redis unavailable, using local cache");
            // Fall back to local cache
            AtomicLong counter = fallbackCache.get(key, k -> new AtomicLong(0));
            return counter.incrementAndGet();
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().trim();
    }
}
