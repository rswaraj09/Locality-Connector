package com.example.localityconnector.security;

import com.example.localityconnector.config.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
 * Stateless JWT security with comprehensive security headers, rate limiting,
 * and role-based access control.
 *
 * <p>Server-rendered page shells are public; the client-side scripts redirect to login
 * when no token is stored. All sensitive behaviour lives behind the role-protected JSON
 * API and is invoked with a bearer token. The bulk {@code /api/business-data/export}
 * dump is restricted to ADMIN.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8081}")
    private String allowedOrigins;

    public SecurityConfig(JwtFilter jwtFilter, RateLimitingFilter rateLimitingFilter) {
        this.jwtFilter = jwtFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Stateless JWT API: CSRF tokens are irrelevant for the REST surface.
                .csrf(csrf -> csrf.disable())
                // ---- Security response headers (OWASP) ----
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(ct -> {})                       // X-Content-Type-Options: nosniff (default)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true))
                        .xssProtection(xss -> xss.headerValue(
                                org.springframework.security.web.header.writers.XXssProtectionHeaderWriter
                                        .HeaderValue.ENABLED_MODE_BLOCK))
                        .referrerPolicy(referrer -> referrer.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
                                        .ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(permissions -> permissions.policy(
                                "camera=(), microphone=(), geolocation=(self), payment=()")))
                .authorizeHttpRequests(auth -> auth
                        // --- Public API + static assets ---
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/directions/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        // Spring Actuator (health + info public; everything else ADMIN)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // OpenAPI / Swagger UI
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**")
                        .permitAll()
                        // Public catalog + business directory + public feedback reads
                        .requestMatchers(HttpMethod.GET, "/api/items/business/**").permitAll()
                        // Export dumps the entire collection: ADMIN only (declared before the public prefix).
                        .requestMatchers(HttpMethod.GET, "/api/business-data/export").hasRole("ADMIN")
                        .requestMatchers("/api/business-data/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/feedback/**").permitAll()
                        // --- Server-rendered page shells (client-side JS guards with the stored JWT) ---
                        .requestMatchers(
                                "/", "/index.html", "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                "/user", "/business", "/user/**", "/business/**",
                                "/user-home", "/user-homepage", "/enhanced-user-dashboard",
                                "/enhanced-business-dashboard", "/business-dashboard",
                                "/listing", "/addlisting", "/feedback", "/update-business",
                                "/admin", "/data-viewer", "/accuracy-test",
                                "/profile", "/forgot-password", "/reset-password")
                        .permitAll()
                        // --- Role-protected JSON API + mutating endpoints ---
                        .requestMatchers(HttpMethod.POST, "/api/feedback").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/feedback/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/feedback/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/user/dashboard/**").hasAnyRole("USER", "BUSINESS")
                        .requestMatchers("/api/user/profile/**").hasAnyRole("USER", "BUSINESS")
                        .requestMatchers("/api/favorites/**").hasRole("USER")
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/items/**").hasRole("BUSINESS")
                        .requestMatchers("/api/business/dashboard/**").hasRole("BUSINESS")
                        .requestMatchers("/api/accuracy/**").hasRole("ADMIN")
                        .requestMatchers("/api/diagnostics/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Rate limiter runs first (before auth), then JWT filter
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
