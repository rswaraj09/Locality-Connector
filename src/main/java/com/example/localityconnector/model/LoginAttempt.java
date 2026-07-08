package com.example.localityconnector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistent brute-force tracking record stored in the Firestore {@code login_attempts}
 * collection. Externalising this state (previously an in-memory {@code ConcurrentHashMap})
 * means a lockout survives restarts and is shared across multiple application instances.
 *
 * <p>Timestamps are stored as epoch milliseconds to keep Firestore (de)serialisation trivial.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt {

    /** Document id == the lowercased email key. */
    private String id;

    private String email;

    private int count;

    private Long firstAttemptEpochMs;

    private Long lockedUntilEpochMs;
}
