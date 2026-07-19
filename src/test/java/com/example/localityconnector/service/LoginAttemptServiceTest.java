package com.example.localityconnector.service;

import com.example.localityconnector.model.LoginAttempt;
import com.example.localityconnector.repository.LoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginAttemptServiceTest {

    /** Mirrors the (private) cap in {@link LoginAttemptService}. */
    private static final int MAX_ATTEMPTS = 5;

    private LoginAttemptRepository repository;
    private LoginAttemptService service;

    /** In-memory backing store for the mocked repository. */
    private final Map<String, LoginAttempt> store = new HashMap<>();

    @BeforeEach
    void setUp() {
        store.clear();
        repository = mock(LoginAttemptRepository.class);

        // Wire up mock behavior to delegate to the in-memory store
        when(repository.findById(anyString())).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            return Optional.ofNullable(store.get(key));
        });
        when(repository.save(any(LoginAttempt.class))).thenAnswer(inv -> {
            LoginAttempt attempt = inv.getArgument(0);
            store.put(attempt.getId(), attempt);
            return attempt;
        });
        doAnswer(inv -> {
            String key = inv.getArgument(0);
            store.remove(key);
            return null;
        }).when(repository).deleteById(anyString());

        service = new LoginAttemptService(repository);
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
