package com.llburgers.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Central Spring Security configuration.
 *
 * <ul>
 *   <li>Stateless JWT-based authentication (no HTTP sessions).</li>
 *   <li>CSRF disabled – relying on SameSite=Strict cookie attribute + HTTPS in prod.</li>
 *   <li>CORS restricted to configured frontend origins with credentials support.</li>
 *   <li>BCrypt cost factor 12 for password hashing.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOriginsRaw;

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    // ─── Beans ────────────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Cost factor 12 — balances security with acceptable response time.
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ─── Filter chain ─────────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Public: auth ────────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()

                // ── Public: email smoke-test endpoint ───────────────────────
                .requestMatchers(HttpMethod.GET, "/api/test/email").permitAll()

                // ── Public: product browsing ────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/sides/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/extras/**").permitAll()

                // ── Public: business status (read-only) ─────────────────────
                .requestMatchers(HttpMethod.GET, "/api/business-status/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/chat").permitAll()

                // ── Public: WebSocket ───────────────────────────────────────
                .requestMatchers("/ws/**").permitAll()

                // ── Admin only ─────────────────────────────────────────────
                .requestMatchers("/api/admins/**").hasAnyRole("ADMIN", "SUPER")
                .requestMatchers("/api/analytics/**").hasAnyRole("ADMIN", "SUPER")
                .requestMatchers("/api/audit/**").hasAnyRole("ADMIN", "SUPER")
                .requestMatchers(HttpMethod.POST, "/api/business-status/**").hasAnyRole("ADMIN", "SUPER")
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "SUPER")

                // ── Everything else requires authentication ──────────────────
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─── CORS ─────────────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOriginsRaw.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true); // Required for HttpOnly cookie exchange
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
