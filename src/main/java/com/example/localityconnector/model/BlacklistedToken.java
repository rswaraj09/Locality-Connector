package com.example.localityconnector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Represents a revoked JWT stored in the Firestore {@code token_blacklist} collection.
 * A token is identified by its unique {@code jti} (JWT ID) claim and carries the
 * original expiry so a scheduled job can purge entries that are no longer relevant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {

    private String id;

    /** The unique JWT ID (jti) claim of the revoked token. */
    private String jti;

    /** When the original token expires; entries past this point can be cleaned up. */
    private Date expiresAt;
}
