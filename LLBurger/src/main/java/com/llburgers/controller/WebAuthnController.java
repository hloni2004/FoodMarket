package com.llburgers.controller;

import com.llburgers.domain.User;
import com.llburgers.dto.AuthResponse;
import com.llburgers.dto.UserSummary;
import com.llburgers.dto.WebAuthnDto;
import com.llburgers.repository.UserRepository;
import com.llburgers.security.JwtService;
import com.llburgers.security.RefreshTokenService;
import com.llburgers.service.WebAuthnService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/webauthn")
public class WebAuthnController {

    private static final Logger log = LoggerFactory.getLogger(WebAuthnController.class);

    private final UserRepository userRepository;
    private final WebAuthnService webAuthnService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public WebAuthnController(UserRepository userRepository,
                              WebAuthnService webAuthnService,
                              JwtService jwtService,
                              RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.webAuthnService = webAuthnService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register/options")
    public ResponseEntity<?> registerOptions(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Not authenticated."));
        }
        User user = userRepository.findByEmail(principal.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return ResponseEntity.ok(webAuthnService.startRegistration(user));
    }

    @PostMapping("/register/finish")
    public ResponseEntity<?> registerFinish(@AuthenticationPrincipal UserDetails principal,
                                            @RequestBody WebAuthnDto.RegistrationFinishRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Not authenticated."));
        }
        try {
            User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
            webAuthnService.finishRegistration(user, request);
            return ResponseEntity.ok(Map.of("message", "Biometric credential registered."));
        } catch (IllegalArgumentException ex) {
            log.warn("[WEBAUTHN-REGISTER] {}", ex.getMessage());
            return ResponseEntity.badRequest().body(error(ex.getMessage()));
        }
    }

    @PostMapping("/login/options")
    public ResponseEntity<?> loginOptions(@RequestBody WebAuthnDto.AuthenticationOptionsRequest request) {
        try {
            return ResponseEntity.ok(webAuthnService.startAuthentication(request.email()));
        } catch (IllegalArgumentException ex) {
            HttpStatus status = "Invalid email or credentials.".equals(ex.getMessage())
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(error(ex.getMessage()));
        }
    }

    @PostMapping("/login/finish")
    public ResponseEntity<?> loginFinish(@RequestBody WebAuthnDto.AuthenticationFinishRequest request,
                                         HttpServletResponse response) {
        try {
            User user = webAuthnService.finishAuthentication(request);
            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(error("Your account has been deactivated. Please contact support."));
            }

            String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                user.getEffectiveRole().name(),
                user.getId()
            );
            String refreshToken = refreshTokenService.createRefreshToken(user);
            addRefreshCookie(response, refreshToken);

            return ResponseEntity.ok(AuthResponse.of(accessToken, UserSummary.from(user)));
        } catch (IllegalArgumentException ex) {
            log.warn("[WEBAUTHN-LOGIN] {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(ex.getMessage()));
        }
    }

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);          // set true in prod (HTTPS only)
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (jwtService.getRefreshTokenExpirationMs() / 1000));
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private Map<String, String> error(String message) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("error", message);
        return map;
    }
}
