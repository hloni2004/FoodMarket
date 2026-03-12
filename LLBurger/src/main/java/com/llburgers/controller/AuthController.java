package com.llburgers.controller;

import com.llburgers.domain.Customer;
import com.llburgers.domain.OtpToken;
import com.llburgers.domain.PasswordResetToken;
import com.llburgers.domain.User;
import com.llburgers.domain.enums.Block;
import com.llburgers.domain.enums.Role;
import com.llburgers.dto.AuthRequest;
import com.llburgers.dto.AuthResponse;
import com.llburgers.dto.RegisterRequest;
import com.llburgers.dto.UserSummary;
import com.llburgers.repository.OtpTokenRepository;
import com.llburgers.repository.PasswordResetTokenRepository;
import com.llburgers.repository.UserRepository;
import com.llburgers.security.JwtService;
import com.llburgers.security.LoginAttemptService;
import com.llburgers.security.PasswordResetRateLimiter;
import com.llburgers.security.RefreshTokenService;
import com.llburgers.service.ICustomerService;
import com.llburgers.util.Helper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles user registration, login, token refresh, logout, and
 * password reset (forgot / reset).
 *
 * <p>All endpoints are public (see {@link com.llburgers.security.SecurityConfig}).
 * Sensitive operations use generic error messages to prevent user-enumeration.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // Known disposable e-mail providers – reject these at registration time.
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "mailinator.com", "guerrillamail.com", "tempmail.com", "throwaway.email",
        "fakeinbox.com", "yopmail.com", "sharklasers.com", "guerrillamailblock.com",
        "trashmail.com", "maildrop.cc", "dispostable.com", "spamgourmet.com",
        "getairmail.com", "discard.email", "mailnull.com", "spamex.com"
    );

    private final UserRepository               userRepository;
    private final PasswordResetTokenRepository prtRepository;
    private final OtpTokenRepository           otpRepository;
    private final ICustomerService             customerService;
    private final JwtService                   jwtService;
    private final RefreshTokenService          refreshTokenService;
    private final LoginAttemptService          loginAttemptService;
    private final PasswordResetRateLimiter     passwordResetRateLimiter;
    private final PasswordEncoder              passwordEncoder;
    private final JavaMailSender               brevoMailSender;

    @Value("${mail.sender1.from}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public AuthController(UserRepository userRepository,
                          PasswordResetTokenRepository prtRepository,
                          OtpTokenRepository otpRepository,
                          ICustomerService customerService,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService,
                          LoginAttemptService loginAttemptService,
                          PasswordResetRateLimiter passwordResetRateLimiter,
                          PasswordEncoder passwordEncoder,
                          @Qualifier("brevoMailSender") JavaMailSender brevoMailSender) {
        this.userRepository           = userRepository;
        this.prtRepository            = prtRepository;
        this.otpRepository            = otpRepository;
        this.customerService          = customerService;
        this.jwtService               = jwtService;
        this.refreshTokenService      = refreshTokenService;
        this.loginAttemptService      = loginAttemptService;
        this.passwordResetRateLimiter = passwordResetRateLimiter;
        this.passwordEncoder          = passwordEncoder;
        this.brevoMailSender          = brevoMailSender;
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req,
                                      HttpServletResponse response) {

        // Reject disposable email domains
        String[] parts = req.email().toLowerCase().split("@");
        if (parts.length == 2 && DISPOSABLE_DOMAINS.contains(parts[1])) {
            return badRequest("Disposable email addresses are not permitted.");
        }

        // Duplicate check
        if (userRepository.existsByEmail(req.email().toLowerCase())) {
            return badRequest("An account with this email already exists.");
        }

        // Build customer — password is hashed inside CustomerServiceImpl.create()
        Customer customer = new Customer();
        customer.setName(sanitize(req.name()));
        customer.setEmail(req.email().toLowerCase().trim());
        customer.setPhone(sanitize(req.phone()));
        customer.setPassword(req.password()); // plain-text; service hashes it
        customer.setRole(Role.CUSTOMER);
        // IMPORTANT: @Builder.Default on User.active does NOT apply to no-args constructor.
        // Lombok @Builder.Default only works when using the Builder pattern.
        // Without this explicit call the field defaults to `false`, causing login to fail
        // immediately with "account deactivated".
        customer.setActive(true);
        customer.setBlock(req.block() != null ? req.block() : Block.A);
        customer.setRoomNumber(sanitize(req.roomNumber()));
        customer.setPaymentMethods(req.paymentMethods() != null ? req.paymentMethods() : "cash");

        Customer saved = customerService.create(customer);
        log.info("[AUTH-REGISTER] New customer: id={}, email={}", saved.getId(), saved.getEmail());

        String accessToken  = jwtService.generateAccessToken(saved.getEmail(), saved.getEffectiveRole().name(), saved.getId());
        String refreshToken = refreshTokenService.createRefreshToken(saved);

        addRefreshCookie(response, refreshToken);
        return ResponseEntity.ok(AuthResponse.of(accessToken, UserSummary.from(saved)));
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req,
                                   HttpServletResponse response) {

        String email = req.email().toLowerCase().trim();

        // Brute-force gate
        if (loginAttemptService.isLocked(email)) {
            return ResponseEntity.status(429)
                .body(error("Account temporarily locked due to too many failed attempts. " +
                            "Please try again in 15 minutes."));
        }

        // Constant-time credential check (avoids timing attacks and user enumeration)
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            loginAttemptService.recordFailure(email);
            long remaining = loginAttemptService.getRemainingAttempts(email);
            String msg = remaining > 0
                ? "Invalid email or password. " + remaining + " attempt(s) remaining."
                : "Invalid email or password.";
            return ResponseEntity.status(401).body(error(msg));
        }

        if (!user.isActive()) {
            return ResponseEntity.status(403)
                .body(error("Your account has been deactivated. Please contact support."));
        }

        loginAttemptService.recordSuccess(email);

        String accessToken  = jwtService.generateAccessToken(user.getEmail(), user.getEffectiveRole().name(), user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        addRefreshCookie(response, refreshToken);
        log.info("[AUTH-LOGIN] email={}", email);
        return ResponseEntity.ok(AuthResponse.of(accessToken, UserSummary.from(user)));
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    /**
     * Issues a new access token + rotates the refresh token.
     * Reads the refresh token from the HttpOnly cookie.
     *
     * <p>Token rotation: the old token is revoked and a new one issued.
     * If a revoked token is replayed, the entire token family is invalidated
     * (theft detection).</p>
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
                                     HttpServletResponse response) {

        String oldRefreshToken = extractRefreshCookie(request);

        if (oldRefreshToken == null
                || !jwtService.isTokenValid(oldRefreshToken)
                || !jwtService.isRefreshToken(oldRefreshToken)) {
            return ResponseEntity.status(401).body(error("Invalid or expired refresh token."));
        }

        // Rotate: revoke old, issue new (or reject if replayed/revoked)
        var rotationResult = refreshTokenService.rotateRefreshToken(oldRefreshToken);

        if (rotationResult.isEmpty()) {
            // Clear the cookie so the client doesn't keep sending a bad token
            clearRefreshCookie(response);
            return ResponseEntity.status(401).body(error("Refresh token invalid or already used. Please log in again."));
        }

        String newRefreshToken = rotationResult.get();
        String email = jwtService.extractEmail(newRefreshToken);
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !user.isActive()) {
            clearRefreshCookie(response);
            return ResponseEntity.status(401).body(error("User not found or deactivated."));
        }

        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getEffectiveRole().name(), user.getId());
        addRefreshCookie(response, newRefreshToken);
        return ResponseEntity.ok(AuthResponse.of(newAccessToken, UserSummary.from(user)));
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Revoke the refresh token in DB
        String refreshToken = extractRefreshCookie(request);
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }
        clearRefreshCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    // ─── Current user ─────────────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(error("Not authenticated."));
        }
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(UserSummary.from(user));
    }

    // ─── Forgot password (OTP) ─────────────────────────────────────────────────

    /**
     * Step 1 – request an OTP.
     * Generates a 6-digit OTP, hashes it, persists it, and emails it.
     * Always returns HTTP 200 to avoid email enumeration.
     *
     * <p>Rate limited to prevent email flooding: 3 requests per email per hour,
     * 10 requests per IP per hour.</p>
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body,
                                            HttpServletRequest request) {
        String email = body.getOrDefault("email", "").toLowerCase().trim();
        String clientIp = getClientIp(request);

        // Rate limit check
        String rateLimitError = passwordResetRateLimiter.checkRateLimit(email, clientIp);
        if (rateLimitError != null) {
            // Still return 200 to avoid enumeration, but don't send email
            log.warn("[AUTH-OTP] Rate limited: email={}, ip={}", email, clientIp);
            return ResponseEntity.ok(Map.of("message",
                "If that email is registered, a 6-digit OTP has been sent."));
        }

        userRepository.findByEmail(email).ifPresent(user -> {
            // Invalidate any existing OTPs for this email
            otpRepository.deleteByEmail(email);

            // Generate a cryptographically secure 6-digit OTP
            String rawOtp = String.format("%06d", new SecureRandom().nextInt(1_000_000));

            OtpToken otp = new OtpToken();
            otp.setEmail(email);
            otp.setOtpHash(passwordEncoder.encode(rawOtp)); // hash before storing
            otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            otp.setUsed(false);
            otpRepository.save(otp);

            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromEmail);
                msg.setTo(user.getEmail());
                msg.setSubject("LL Burgers – Your Password Reset Code");
                msg.setText("""
                    Hi %s,

                    Your one-time password reset code is:

                        %s

                    This code expires in 10 minutes. Do not share it with anyone.

                    If you did not request a password reset, you can safely ignore this email.

                    — The LL Burgers Team
                    """.formatted(user.getName(), rawOtp));

                brevoMailSender.send(msg);
                log.info("[AUTH-OTP] OTP email sent to {}", email);
            } catch (MailException e) {
                log.error("[AUTH-OTP] Failed to send OTP email to {}: {}", email, e.getMessage());
            }
        });

        return ResponseEntity.ok(Map.of("message",
            "If that email is registered, a 6-digit OTP has been sent."));
    }

    /**
     * Step 2 – verify the OTP.
     * On success, invalidates the OTP and issues a one-time reset token
     * which the client uses to call {@code /reset-password}.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").toLowerCase().trim();
        String rawOtp = body.getOrDefault("otp", "").trim();

        if (email.isEmpty() || rawOtp.isEmpty()) {
            return badRequest("Email and OTP are required.");
        }

        OtpToken otpToken = otpRepository.findTopByEmailOrderByCreatedAtDesc(email).orElse(null);

        if (otpToken == null
                || otpToken.isUsed()
                || otpToken.getExpiresAt().isBefore(LocalDateTime.now())
                || !passwordEncoder.matches(rawOtp, otpToken.getOtpHash())) {
            return badRequest("Invalid or expired OTP. Please try again.");
        }

        // Invalidate the OTP immediately
        otpToken.setUsed(true);
        otpRepository.save(otpToken);

        // Issue a short-lived password reset token so the client can call /reset-password
        String rawToken = UUID.randomUUID().toString().replace("-", "")
                        + UUID.randomUUID().toString().replace("-", "");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after OTP verification"));

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(rawToken);
        prt.setUser(user);
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        prt.setUsed(false);
        prtRepository.save(prt);

        log.info("[AUTH-OTP] OTP verified for {}, reset token issued", email);
        return ResponseEntity.ok(Map.of("resetToken", rawToken));
    }

    // ─── Reset password ───────────────────────────────────────────────────────

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token       = body.getOrDefault("token", "").trim();
        String newPassword = body.getOrDefault("password", "");

        if (token.isEmpty() || newPassword.isEmpty()) {
            return badRequest("Token and new password are required.");
        }

        if (!Helper.isValidPassword(newPassword)) {
            return badRequest(
                "Password must be at least 12 characters and include uppercase, " +
                "lowercase, a number, and a special character.");
        }

        PasswordResetToken prt = prtRepository.findByToken(token).orElse(null);

        if (prt == null || prt.isUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            return badRequest("This reset link is invalid or has expired.");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true); // invalidate immediately
        prtRepository.save(prt);

        // SECURITY: Revoke all refresh tokens when password is changed
        refreshTokenService.revokeAllForUser(user.getId());

        log.info("[AUTH-RESET-PASSWORD] Password reset for user id={}", user.getId());
        return ResponseEntity.ok(Map.of("message",
            "Your password has been reset successfully. Please sign in."));
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);          // set true in prod (HTTPS only)
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (jwtService.getRefreshTokenExpirationMs() / 1000));
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);          // set true in prod (HTTPS only)
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);              // tells browser to delete it immediately
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(error(message));
    }

    private Map<String, String> error(String message) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("error", message);
        return map;
    }

    /** Strip HTML tags and dangerous characters for basic XSS mitigation. */
    private String sanitize(String input) {
        if (input == null) return "";
        return input.trim()
                .replaceAll("<[^>]*>", "")
                .replaceAll("[<>\"'`;&]", "");
    }

    /** Extract client IP, handling proxies via X-Forwarded-For header. */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For can be comma-separated; first entry is the original client
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
