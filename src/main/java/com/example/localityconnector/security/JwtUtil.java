package com.example.localityconnector.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Issues and verifies stateless JWTs.
 *
 * <p>Each token carries the full identity required by the application so that no
 * server-side {@code HttpSession} is needed:</p>
 * <ul>
 *   <li>{@code sub}      - the login email</li>
 *   <li>{@code role}     - the primary role (USER / BUSINESS)</li>
 *   <li>{@code roles}    - the full set of granted roles (may include ADMIN)</li>
 *   <li>{@code sub_id}   - the entity id (businessId or userId)</li>
 *   <li>{@code sub_name} - the display name (businessName or userName)</li>
 *   <li>{@code jti}      - a unique token id used for blacklisting on logout</li>
 * </ul>
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private static final long JWT_TOKEN_VALIDITY = 10 * 60 * 60 * 1000L; // 10 hours

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be configured and at least 32 bytes long. Set the JWT_SECRET_KEY environment variable.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object raw = extractClaim(token, claims -> claims.get("roles"));
        if (raw instanceof Collection<?> collection) {
            List<String> roles = new ArrayList<>();
            for (Object value : collection) {
                if (value != null) {
                    roles.add(value.toString());
                }
            }
            return roles;
        }
        String single = extractRole(token);
        return single == null ? List.of() : List.of(single);
    }

    public String extractEntityId(String token) {
        return extractClaim(token, claims -> claims.get("sub_id", String.class));
    }

    public String extractEntityName(String token) {
        return extractClaim(token, claims -> claims.get("sub_name", String.class));
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generate a token for the given identity.
     *
     * @param email      login email (token subject)
     * @param roles      one or more roles to grant (e.g. {@code USER}, {@code USER+ADMIN})
     * @param entityId   the businessId or userId
     * @param entityName the businessName or userName
     */
    public String generateToken(String email, Collection<String> roles, String entityId, String entityName) {
        Map<String, Object> claims = new HashMap<>();
        String primaryRole = roles.stream()
                .filter(r -> !"ADMIN".equals(r))
                .findFirst()
                .orElseGet(() -> roles.iterator().next());
        claims.put("role", primaryRole);
        claims.put("roles", new ArrayList<>(roles));
        claims.put("sub_id", entityId);
        claims.put("sub_name", entityName);
        return createToken(claims, email);
    }

    /** Convenience overload for a single-role token. */
    public String generateToken(String email, String role, String entityId, String entityName) {
        return generateToken(email, List.of(role), entityId, entityName);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
