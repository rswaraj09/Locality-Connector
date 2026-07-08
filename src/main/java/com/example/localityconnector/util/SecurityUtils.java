package com.example.localityconnector.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper for reading the authenticated identity from the Spring Security context.
 *
 * <p>The application is fully JWT-stateless: identity (entity id), display name and
 * role are populated onto the {@link Authentication} by the JWT filter and are read
 * back here. No {@code HttpSession} is involved.</p>
 */
public final class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /** @return the logged-in entity id (businessId or userId), or {@code null} if unauthenticated. */
    public static String getLoggedInEntityId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        return principal.toString();
    }

    /** @return the display name (businessName or userName) carried by the token, or {@code null}. */
    public static String getLoggedInEntityName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object details = authentication.getDetails();
        return (details instanceof String) ? (String) details : null;
    }

    /** @return the primary role (USER / BUSINESS / ADMIN) without the {@code ROLE_} prefix, or {@code null}. */
    public static String getLoggedInRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role != null && role.startsWith("ROLE_")) {
                String stripped = role.substring("ROLE_".length());
                // Prefer a non-ADMIN primary role when both are present.
                if (!"ADMIN".equals(stripped)) {
                    return stripped;
                }
            }
        }
        // Fall back to whatever role is present (e.g. ADMIN-only principals).
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role != null && role.startsWith("ROLE_")) {
                return role.substring("ROLE_".length());
            }
        }
        return null;
    }

    public static boolean isBusinessLoggedIn() {
        return hasRole("BUSINESS");
    }

    public static boolean isUserLoggedIn() {
        return hasRole("USER");
    }

    public static boolean isAdminLoggedIn() {
        return hasRole("ADMIN");
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (("ROLE_" + role).equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
