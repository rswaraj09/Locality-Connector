package com.example.localityconnector.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "test-only-secret-value-that-is-long-enough-for-jwt-hs256";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    @Test
    void constructor_rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil("too-short"));
    }

    @Test
    void generateAndParse_roundTripsAllClaims() {
        String token = jwtUtil.generateToken("user@example.com", List.of("USER"), "entity-1", "Jane Doe");

        assertEquals("user@example.com", jwtUtil.extractUsername(token));
        assertEquals("entity-1", jwtUtil.extractEntityId(token));
        assertEquals("Jane Doe", jwtUtil.extractEntityName(token));
        assertTrue(jwtUtil.extractRoles(token).contains("USER"));
        assertNotNull(jwtUtil.extractJti(token));
        assertTrue(jwtUtil.validateToken(token, "user@example.com"));
    }

    @Test
    void validateToken_matchesUsername() {
        String token = jwtUtil.generateToken("biz@example.com", List.of("BUSINESS"), "b-1", "Acme");
        assertTrue(jwtUtil.validateToken(token, "biz@example.com"));
        assertFalse(jwtUtil.validateToken(token, "someone-else@example.com"));
    }

    @Test
    void uniqueJtiPerToken() {
        String t1 = jwtUtil.generateToken("a@example.com", Set.of("USER"), "e1", "A");
        String t2 = jwtUtil.generateToken("a@example.com", Set.of("USER"), "e1", "A");
        assertNotEquals(jwtUtil.extractJti(t1), jwtUtil.extractJti(t2));
    }

    @Test
    void primaryRoleClaimPrefersNonAdmin() {
        String token = jwtUtil.generateToken("admin@example.com", List.of("USER", "ADMIN"), "e1", "Admin");
        assertTrue(jwtUtil.extractRoles(token).contains("ADMIN"));
        assertTrue(jwtUtil.extractRoles(token).contains("USER"));
        assertEquals("USER", jwtUtil.extractRole(token));
    }

    @Test
    void tamperedToken_isRejected() {
        String token = jwtUtil.generateToken("user@example.com", List.of("USER"), "e1", "U");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("a") ? "bb" : "aa");
        // A tampered signature must never validate: either it throws on parse or returns false.
        assertThrows(Exception.class, () -> jwtUtil.validateToken(tampered, "user@example.com"));
    }
}
