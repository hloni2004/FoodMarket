package com.llburgers.config;

import com.llburgers.security.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-WebSocket configuration with SockJS fallback.
 *
 * <h3>Connection endpoint</h3>
 * <pre>
 *   ws://host/ws          (native WebSocket)
 *   http://host/ws        (SockJS long-polling fallback)
 * </pre>
 *
 * <h3>Subscription destinations (client SUBSCRIBE)</h3>
 * <pre>
 *   /topic/orders                  – all order events  (admin dashboard)
 *   /topic/order/{orderId}         – single-order updates (customer order-tracker)
 *   /topic/business                – business open / close broadcast
 *   /user/queue/notifications      – personal queue per connected user
 * </pre>
 *
 * <h3>Application destinations (client SEND / server convertAndSend)</h3>
 * <pre>
 *   /app/...  → routes to @MessageMapping methods (reserved for future use)
 * </pre>
 *
 * <h3>Authentication</h3>
 * <p>Clients must include a valid JWT access token in the STOMP CONNECT frame:</p>
 * <pre>
 *   CONNECT
 *   Authorization: Bearer &lt;token&gt;
 * </pre>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Allow all origins in dev; lock down to your frontend domain in prod
                .setAllowedOriginPatterns("*")
                .withSockJS();          // SockJS fallback for browsers without native WS
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker handles /topic/* and /queue/*
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages sent from clients to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix that SimpMessagingTemplate uses for user-specific queues
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register the auth interceptor to validate JWT tokens on CONNECT
        registration.interceptors(authInterceptor);
    }
}
