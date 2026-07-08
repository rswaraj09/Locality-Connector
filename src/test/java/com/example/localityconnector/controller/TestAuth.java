package com.example.localityconnector.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Test helper that builds an authentication mirroring the production {@code JwtFilter}:
 * the principal is the entity id (businessId / userId) as a plain String and the granted
 * authorities are {@code ROLE_*}. Using a String principal (instead of {@code @WithMockUser},
 * whose principal is a {@code UserDetails} object) keeps {@code SecurityUtils.getLoggedInEntityId()}
 * returning the raw id that controllers rely on for ownership checks.
 */
final class TestAuth {

    private TestAuth() {
    }

    static RequestPostProcessor principal(String entityId, String... roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(entityId, null, authorities);
        token.setDetails("Test Entity");
        return authentication(token);
    }
}
