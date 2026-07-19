package com.example.localityconnector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Represents a revoked JWT stored in the {@code token_blacklist} collection.
 * A token is identified by its unique {@code jti} (JWT ID) claim and carries the
 * original expiry so a scheduled job can purge entries that are no longer relevant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "token_blacklist")
public class BlacklistedToken {

    @Id
    private String id;

    /** The unique JWT ID (jti) claim of the revoked token. */
    @Indexed(unique = true)
    private String jti;

    /** When the original token expires; entries past this point can be cleaned up. */
    @Indexed
    private Date expiresAt;
}
