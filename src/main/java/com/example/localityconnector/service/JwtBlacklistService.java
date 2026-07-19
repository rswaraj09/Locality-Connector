package com.example.localityconnector.service;

import com.example.localityconnector.model.BlacklistedToken;
import com.example.localityconnector.repository.JwtBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Manages revoked JWTs. A token is blacklisted by its {@code jti} until its original
 * expiry, after which a scheduled job purges it from MongoDB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final JwtBlacklistRepository blacklistRepository;

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        return blacklistRepository.existsByJti(jti);
    }

    public void blacklist(String jti, Date expiresAt) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        blacklistRepository.save(new BlacklistedToken(null, jti, expiresAt));
    }

    /** Runs hourly to remove blacklist entries whose tokens have already expired. */
    @Scheduled(fixedRate = 60 * 60 * 1000L)
    public void cleanupExpiredTokens() {
        try {
            long removed = blacklistRepository.deleteByExpiresAtBefore(new Date());
            if (removed > 0) {
                log.info("Purged {} expired token-blacklist entries", removed);
            }
        } catch (Exception e) {
            log.warn("Token blacklist cleanup failed: {}", e.getMessage());
        }
    }
}
