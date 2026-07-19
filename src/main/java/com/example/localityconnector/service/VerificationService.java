package com.example.localityconnector.service;

import com.example.localityconnector.model.VerificationToken;
import com.example.localityconnector.repository.VerificationTokenRepository;
import com.example.localityconnector.repository.UserRepository;
import com.example.localityconnector.repository.BusinessRepository;
import com.example.localityconnector.model.User;
import com.example.localityconnector.model.Business;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages email verification tokens. Tokens expire after 24 hours and are
 * cleaned up by a scheduled task.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final long TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000L; // 24 hours

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final EmailService emailService;

    /**
     * Create a verification token and send a verification email.
     *
     * @return the generated token string
     */
    public String createAndSendVerification(String entityId, String entityType, String email) {
        String tokenStr = UUID.randomUUID().toString();

        VerificationToken token = new VerificationToken();
        token.setEntityId(entityId);
        token.setEntityType(entityType);
        token.setEmail(email);
        token.setToken(tokenStr);
        token.setExpiresAt(new Date(System.currentTimeMillis() + TOKEN_VALIDITY_MS));
        token.prePersist();
        tokenRepository.save(token);

        try {
            emailService.sendVerificationEmail(email, tokenStr);
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}: {}", email, e.getMessage());
        }

        return tokenStr;
    }

    /**
     * Verify a token. If valid, marks the user/business as email-verified.
     *
     * @return true if verification succeeded
     */
    public boolean verify(String tokenStr) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(tokenStr);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        VerificationToken token = tokenOpt.get();
        if (token.isUsed() || token.getExpiresAt().before(new Date())) {
            return false;
        }

        // Mark token as used
        token.setUsed(true);
        tokenRepository.save(token);

        // Mark entity as verified
        if ("USER".equals(token.getEntityType())) {
            userRepository.findById(token.getEntityId()).ifPresent(user -> {
                user.setEmailVerified(true);
                user.prePersist();
                userRepository.save(user);
            });
        } else if ("BUSINESS".equals(token.getEntityType())) {
            businessRepository.findById(token.getEntityId()).ifPresent(business -> {
                business.setEmailVerified(true);
                business.prePersist();
                businessRepository.save(business);
            });
        }

        return true;
    }

    /** Periodic cleanup of expired verification tokens. */
    @Scheduled(fixedRate = 3600_000) // every hour
    public void cleanupExpiredTokens() {
        long deleted = tokenRepository.deleteByExpiresAtBefore(new Date());
        if (deleted > 0) {
            log.info("Cleaned up {} expired verification tokens", deleted);
        }
    }
}
