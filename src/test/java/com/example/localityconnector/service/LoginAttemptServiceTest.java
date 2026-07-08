package com.example.localityconnector.service;

import com.example.localityconnector.model.LoginAttempt;
import com.example.localityconnector.repository.LoginAttemptFirestoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    /** Mirrors the (private) cap in {@link LoginAttemptService}. */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * In-memory stand-in for the Firestore-backed repository so the service can be
     * unit-tested without a live Firebase connection. Possible now that the repository
     * is constructor-injected.
     */
    static class FakeLoginAttemptRepository extends LoginAttemptFirestoreRepository {
        private final Map<String, LoginAttempt> store = new HashMap<>();

        FakeLoginAttemptRepository() {
            super(null);
        }

        @Override
        public Optional<LoginAttempt> findByKey(String key) {
            return Optional.ofNullable(store.get(key));
        }

        @Override
        public void save(String key, LoginAttempt attempt) {
            attempt.setId(key);
            store.put(key, attempt);
        }

        @Override
        public void deleteByKey(String key) {
            store.remove(key);
        }

        @Override
        public void recordFailedAttemptAtomically(String key, long windowMillis, int maxAttempts, long lockMillis) {
            long now = java.time.Instant.now().toEpochMilli();
            LoginAttempt record = findByKey(key).orElseGet(() -> {
                LoginAttempt fresh = new LoginAttempt();
                fresh.setId(key);
                fresh.setEmail(key);
                fresh.setCount(0);
                fresh.setFirstAttemptEpochMs(now);
                return fresh;
            });

            if (record.getFirstAttemptEpochMs() == null
                    || (now - record.getFirstAttemptEpochMs()) >= windowMillis) {
                record.setFirstAttemptEpochMs(now);
                record.setCount(0);
                record.setLockedUntilEpochMs(null);
            }
            record.setCount(record.getCount() + 1);
            if (record.getCount() >= maxAttempts) {
                record.setLockedUntilEpochMs(now + lockMillis);
            }
            save(key, record);
        }
    }

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(new FakeLoginAttemptRepository());
    }

    @Test
    void notLockedInitially() {
        assertFalse(service.isLocked("user@example.com"));
        assertEquals(0, service.getRetryAfterSeconds("user@example.com"));
    }

    @Test
    void locksAfterMaxFailures() {
        String email = "user@example.com";
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            assertFalse(service.isLocked(email), "should not lock before reaching the cap");
            service.loginFailed(email);
        }
        assertTrue(service.isLocked(email));
        assertTrue(service.getRetryAfterSeconds(email) > 0);
    }

    @Test
    void successResetsCounter() {
        String email = "user@example.com";
        service.loginFailed(email);
        service.loginFailed(email);
        service.loginSucceeded(email);
        assertFalse(service.isLocked(email));
        assertEquals(0, service.getRetryAfterSeconds(email));
    }

    @Test
    void emailMatchingIsCaseInsensitive() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.loginFailed("User@Example.com");
        }
        assertTrue(service.isLocked("user@example.com"));
    }

    @Test
    void nullEmailIsSafe() {
        assertFalse(service.isLocked(null));
        assertEquals(0, service.getRetryAfterSeconds(null));
        service.loginFailed(null);
        service.loginSucceeded(null);
    }
}
