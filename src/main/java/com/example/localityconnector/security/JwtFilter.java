package com.example.localityconnector.security;

import com.example.localityconnector.service.JwtBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateless JWT authentication filter.
 *
 * <p>Validates the bearer token using {@link JwtUtil} only, rejects revoked
 * (blacklisted) tokens, and populates a {@link UsernamePasswordAuthenticationToken}
 * whose <b>principal is the entity id</b> (businessId/userId) and whose authorities
 * are derived from the token's role claims. The display name is stored as the
 * authentication detail. No {@code HttpSession} is used.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Token invalid or expired - leave the context unauthenticated.
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt, username)) {

                // Reject revoked tokens (logout / blacklist).
                String jti = null;
                try {
                    jti = jwtUtil.extractJti(jwt);
                } catch (Exception ignored) {
                    // No jti present.
                }
                if (jti != null && jwtBlacklistService.isBlacklisted(jti)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"error\":\"Token has been revoked\"}");
                    return;
                }

                List<String> roles;
                try {
                    roles = jwtUtil.extractRoles(jwt);
                } catch (Exception ignored) {
                    roles = List.of();
                }

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                for (String role : roles) {
                    if (role != null && !role.isBlank()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }

                String entityId = jwtUtil.extractEntityId(jwt);
                String entityName = jwtUtil.extractEntityName(jwt);

                // Principal is the entity id so controllers can read it via SecurityUtils.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        entityId != null ? entityId : username, null, authorities);
                // Store the display name as the authentication detail.
                authentication.setDetails(entityName);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip token processing for unauthenticated entry points and server-rendered portal pages.
        return path.startsWith("/api/auth/login") || path.startsWith("/api/auth/user/")
                || path.startsWith("/api/auth/business/")
                || path.startsWith("/user/") || path.startsWith("/business/")
                || path.equals("/user") || path.equals("/business");
    }
}
