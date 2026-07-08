package com.example.localityconnector.scheduler;

import com.example.localityconnector.repository.JwtBlacklistFirestoreRepository;
import com.example.localityconnector.repository.PasswordResetTokenFirestoreRepository;
import com.example.localityconnector.repository.VerificationTokenFirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final JwtBlacklistFirestoreRepository blacklistRepository;
    private final VerificationTokenFirestoreRepository verificationRepository;
    private final PasswordResetTokenFirestoreRepository resetRepository;

    /**
     * Run nightly at 3:00 AM server time to purge expired tokens from Firestore.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void purgeExpiredTokens() {
        log.info("Starting scheduled cleanup of expired tokens...");
        try {
            int blacklistedPurged = blacklistRepository.deleteExpired();
            int verificationPurged = verificationRepository.deleteExpired();
            int resetPurged = resetRepository.deleteExpired();
            log.info("Token cleanup completed. Purged {} blacklisted JWTs, {} verification tokens, and {} password reset tokens.",
                    blacklistedPurged, verificationPurged, resetPurged);
        } catch (Exception e) {
            log.error("Error occurred during scheduled token cleanup: {}", e.getMessage(), e);
        }
    }
}
