package com.example.localityconnector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Persistent brute-force tracking record stored in the {@code login_attempts}
 * collection. Externalising this state means a lockout survives restarts
 * and is shared across multiple application instances.
 *
 * <p>Timestamps are stored as epoch milliseconds.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "login_attempts")
public class LoginAttempt {

    /** Document id == the lowercased email key. */
    @Id
    private String id;

    @Indexed
    private String email;

    private int count;

    private Long firstAttemptEpochMs;

    private Long lockedUntilEpochMs;
}
