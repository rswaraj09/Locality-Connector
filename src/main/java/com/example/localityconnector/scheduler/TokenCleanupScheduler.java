package com.example.localityconnector.scheduler;

import com.example.localityconnector.repository.JwtBlacklistRepository;
import com.example.localityconnector.repository.PasswordResetTokenRepository;
import com.example.localityconnector.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final JwtBlacklistRepository blacklistRepository;
    private final VerificationTokenRepository verificationRepository;
    private final PasswordResetTokenRepository resetRepository;

    /**
     * Run nightly at 3:00 AM server time to purge expired tokens from MongoDB.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void purgeExpiredTokens() {
        log.info("Starting scheduled cleanup of expired tokens...");
        try {
            Date now = new Date();
            long blacklistedPurged = blacklistRepository.deleteByExpiresAtBefore(now);
            long verificationPurged = verificationRepository.deleteByExpiresAtBefore(now);
            long resetPurged = resetRepository.deleteByExpiresAtBefore(now);
            log.info("Token cleanup completed. Purged {} blacklisted JWTs, {} verification tokens, and {} password reset tokens.",
                    blacklistedPurged, verificationPurged, resetPurged);
        } catch (Exception e) {
            log.error("Error occurred during scheduled token cleanup: {}", e.getMessage(), e);
        }
    }
}
