package com.example.localityconnector.service;

import com.example.localityconnector.model.LoginAttempt;
import com.example.localityconnector.repository.LoginAttemptFirestoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Firestore-backed brute-force protection. Failed-login state is persisted in the
 * {@code login_attempts} collection so a lockout survives application restarts and is
 * shared across multiple instances. The previous implementation used an in-memory
 * {@link java.util.concurrent.ConcurrentHashMap}, which was per-instance and lost on
 * restart.
 *
 * <p>An account is locked for {@value #LOCK_MINUTES} minutes after {@value #MAX_ATTEMPTS}
 * failures within a {@value #WINDOW_MINUTES}-minute window.</p>
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MINUTES = 15;
    private static final long LOCK_MINUTES = 30;

    private final LoginAttemptFirestoreRepository repository;

    public boolean isLocked(String email) {
        if (email == null) {
            return false;
        }
        Optional<LoginAttempt> recordOpt = repository.findByKey(key(email));
        if (recordOpt.isEmpty() || recordOpt.get().getLockedUntilEpochMs() == null) {
            return false;
        }
        if (Instant.now().toEpochMilli() < recordOpt.get().getLockedUntilEpochMs()) {
            return true;
        }
        // Lockout expired - clear it.
        repository.deleteByKey(key(email));
        return false;
    }

    public void loginFailed(String email) {
        if (email == null) {
            return;
        }
        String key = key(email);
        long windowMillis = Duration.ofMinutes(WINDOW_MINUTES).toMillis();
        long lockMillis = Duration.ofMinutes(LOCK_MINUTES).toMillis();
        repository.recordFailedAttemptAtomically(key, windowMillis, MAX_ATTEMPTS, lockMillis);
    }

    public void loginSucceeded(String email) {
        if (email != null) {
            repository.deleteByKey(key(email));
        }
    }

    /** @return seconds remaining until the account unlocks, or 0 if not locked. */
    public long getRetryAfterSeconds(String email) {
        if (email == null) {
            return 0;
        }
        Optional<LoginAttempt> recordOpt = repository.findByKey(key(email));
        if (recordOpt.isEmpty() || recordOpt.get().getLockedUntilEpochMs() == null) {
            return 0;
        }
        long seconds = (recordOpt.get().getLockedUntilEpochMs() - Instant.now().toEpochMilli()) / 1000;
        return Math.max(seconds, 0);
    }

    private String key(String email) {
        return email.toLowerCase();
    }
}
