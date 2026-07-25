package com.example.localityconnector.service;

import com.example.localityconnector.model.OtpToken;
import com.example.localityconnector.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;

/**
 * Manages 4-digit OTP generation and verification for Email and Phone numbers during registration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpVerificationService {

    private static final long OTP_VALIDITY_MS = 10 * 60 * 1000L; // 10 minutes
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    /**
     * Generates a 4-digit OTP (1000-9999) and dispatches it via Email (Resend API) or logs it for SMS.
     */
    public String sendOtp(String target, String targetType) {
        String cleanTarget = target != null ? target.trim().toLowerCase() : "";
        if (cleanTarget.isEmpty()) {
            throw new IllegalArgumentException("Target email or phone number is required");
        }

        // Generate 4-digit code (1000 - 9999)
        int codeInt = 1000 + RANDOM.nextInt(9000);
        String otpCode = String.valueOf(codeInt);

        OtpToken token = new OtpToken();
        token.setTarget(cleanTarget);
        token.setTargetType(targetType != null ? targetType.toUpperCase() : "EMAIL");
        token.setOtpCode(otpCode);
        token.setExpiresAt(new Date(System.currentTimeMillis() + OTP_VALIDITY_MS));
        token.prePersist();
        otpTokenRepository.save(token);

        if ("PHONE".equalsIgnoreCase(targetType)) {
            log.info("[SMS OTP] Sent 4-digit OTP {} to phone number {}", otpCode, cleanTarget);
        } else {
            emailService.sendRegistrationOtpEmail(cleanTarget, otpCode);
        }

        return otpCode;
    }

    /**
     * Verifies the 4-digit OTP for a given email or phone number.
     */
    public boolean verifyOtp(String target, String otpCode) {
        if (target == null || otpCode == null) return false;
        String cleanTarget = target.trim().toLowerCase();
        String cleanCode = otpCode.trim();

        Optional<OtpToken> tokenOpt = otpTokenRepository.findTopByTargetAndOtpCodeOrderByCreatedAtDesc(cleanTarget, cleanCode);
        if (tokenOpt.isEmpty()) {
            log.warn("OTP verification failed: no matching token for target={} code={}", cleanTarget, cleanCode);
            return false;
        }

        OtpToken token = tokenOpt.get();
        if (token.getExpiresAt().before(new Date())) {
            log.warn("OTP verification failed: token expired for target={}", cleanTarget);
            return false;
        }

        token.setVerified(true);
        otpTokenRepository.save(token);
        log.info("OTP successfully verified for target={}", cleanTarget);
        return true;
    }

    /**
     * Checks if the email or phone target has been verified via OTP.
     */
    public boolean isTargetVerified(String target) {
        if (target == null || target.trim().isEmpty()) return false;
        String cleanTarget = target.trim().toLowerCase();
        Optional<OtpToken> tokenOpt = otpTokenRepository.findTopByTargetAndTargetTypeOrderByCreatedAtDesc(cleanTarget, "EMAIL");
        if (tokenOpt.isPresent() && tokenOpt.get().isVerified()) {
            return true;
        }
        Optional<OtpToken> phoneOpt = otpTokenRepository.findTopByTargetAndTargetTypeOrderByCreatedAtDesc(cleanTarget, "PHONE");
        return phoneOpt.isPresent() && phoneOpt.get().isVerified();
    }

    @Scheduled(fixedRate = 1800_000) // 30 mins
    public void cleanupExpiredOtps() {
        long deleted = otpTokenRepository.deleteByExpiresAtBefore(new Date());
        if (deleted > 0) {
            log.info("Cleaned up {} expired OTP tokens", deleted);
        }
    }
}
