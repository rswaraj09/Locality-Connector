package com.example.localityconnector.service;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.PasswordResetToken;
import com.example.localityconnector.model.User;
import com.example.localityconnector.repository.BusinessFirestoreRepository;
import com.example.localityconnector.repository.PasswordResetTokenFirestoreRepository;
import com.example.localityconnector.repository.UserFirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages password reset tokens. Tokens expire after 1 hour.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final long TOKEN_VALIDITY_MS = 60 * 60 * 1000L; // 1 hour

    private final PasswordResetTokenFirestoreRepository tokenRepository;
    private final UserFirestoreRepository userRepository;
    private final BusinessFirestoreRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Request a password reset. Looks up both user and business collections.
     * Always returns success to prevent email enumeration.
     */
    public void requestReset(String email) {
        String entityType = null;

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            entityType = "USER";
        } else {
            Optional<Business> businessOpt = businessRepository.findByEmail(email);
            if (businessOpt.isPresent()) {
                entityType = "BUSINESS";
            }
        }

        if (entityType == null) {
            // Don't reveal whether the email exists
            log.info("Password reset requested for unknown email: {}", email);
            return;
        }

        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setEntityType(entityType);
        token.setToken(tokenStr);
        token.setExpiresAt(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_MS));
        token.prePersist();
        tokenRepository.save(token);

        try {
            emailService.sendPasswordResetEmail(email, tokenStr);
        } catch (Exception e) {
            log.warn("Failed to send password reset email to {}: {}", email, e.getMessage());
        }
    }

    /**
     * Reset password using a valid token.
     *
     * @return true if the reset succeeded
     */
    public boolean resetPassword(String tokenStr, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(tokenStr);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOpt.get();
        if (token.isUsed() || token.getExpiresAt().before(new Date())) {
            return false;
        }

        // Mark token as used
        token.setUsed(true);
        tokenRepository.save(token);

        String encodedPassword = passwordEncoder.encode(newPassword);

        if ("USER".equals(token.getEntityType())) {
            userRepository.findByEmail(token.getEmail()).ifPresent(user -> {
                user.setPassword(encodedPassword);
                user.prePersist();
                userRepository.save(user);
            });
        } else if ("BUSINESS".equals(token.getEntityType())) {
            businessRepository.findByEmail(token.getEmail()).ifPresent(business -> {
                business.setPassword(encodedPassword);
                business.prePersist();
                businessRepository.save(business);
            });
        }

        return true;
    }

    /** Periodic cleanup of expired reset tokens. */
    @Scheduled(fixedRate = 3600_000)
    public void cleanupExpiredTokens() {
        int deleted = tokenRepository.deleteExpired();
        if (deleted > 0) {
            log.info("Cleaned up {} expired password reset tokens", deleted);
        }
    }
}
