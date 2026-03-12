package com.llburgers.security;

import com.llburgers.domain.User;
import com.llburgers.domain.enums.Role;
import com.llburgers.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Intercepts STOMP CONNECT frames and validates the JWT access token.
 *
 * <p>Clients must send the access token in the STOMP header:</p>
 * <pre>
 *   CONNECT
 *   Authorization: Bearer &lt;token&gt;
 * </pre>
 *
 * <p>If valid, the user principal is populated so topic subscriptions
 * can be authorized and user-specific queues work correctly.</p>
 *
 * <h2>Security Model</h2>
 * <ul>
 *   <li>CONNECT without a valid token is rejected</li>
 *   <li>User info is cached in the WebSocket session</li>
 *   <li>Enables future per-topic authorization (e.g., admins only for /topic/orders)</li>
 * </ul>
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public WebSocketAuthInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Only intercept CONNECT frames
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("[WS-AUTH] No Authorization header in CONNECT frame");
                // Allow anonymous connection for public topics, but without user principal
                return message;
            }

            String token = authHeader.substring(7).trim();

            try {
                if (!jwtService.isTokenValid(token) || !jwtService.isAccessToken(token)) {
                    log.warn("[WS-AUTH] Invalid or non-access token in CONNECT");
                    throw new SecurityException("Invalid access token");
                }

                String email = jwtService.extractEmail(token);
                User user = userRepository.findByEmail(email).orElse(null);

                if (user == null || !user.isActive()) {
                    log.warn("[WS-AUTH] User not found or inactive: {}", email);
                    throw new SecurityException("User not found or inactive");
                }

                // Create authentication with authorities
                // Super admins get both ADMIN and SUPER roles for full permission inheritance
                List<SimpleGrantedAuthority> authorities;
                if (user.getEffectiveRole() == Role.SUPER) {
                    authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_SUPER")
                    );
                } else {
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                }
                
                var auth = new UsernamePasswordAuthenticationToken(
                        user.getEmail(),  // principal
                        null,             // credentials (not needed)
                        authorities
                );

                // Store in the accessor so it's available for the session
                accessor.setUser(auth);
                log.debug("[WS-AUTH] Authenticated WebSocket user: {}", email);

            } catch (SecurityException e) {
                throw e;
            } catch (Exception e) {
                log.warn("[WS-AUTH] Token validation failed: {}", e.getMessage());
                throw new SecurityException("Authentication failed");
            }
        }

        return message;
    }
}
