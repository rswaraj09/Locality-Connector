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

    @org.springframework.beans.factory.annotation.Value("${fast2sms.api.key:${FAST2SMS_API_KEY:}}")
    private String fast2smsApiKey;

    @org.springframework.beans.factory.annotation.Value("${twilio.account.sid:${TWILIO_ACCOUNT_SID:}}")
    private String twilioAccountSid;

    @org.springframework.beans.factory.annotation.Value("${twilio.auth.token:${TWILIO_AUTH_TOKEN:}}")
    private String twilioAuthToken;

    @org.springframework.beans.factory.annotation.Value("${twilio.phone.number:${TWILIO_PHONE_NUMBER:}}")
    private String twilioPhoneNumber;

    private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

    /**
     * Generates a 4-digit OTP (1000-9999) and dispatches it via Email (Resend API) or SMS (Twilio / Fast2SMS API).
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
            dispatchSmsOtp(cleanTarget, otpCode);
        } else {
            emailService.sendRegistrationOtpEmail(cleanTarget, otpCode);
        }

        return otpCode;
    }

    private void dispatchSmsOtp(String rawPhone, String otpCode) {
        if (twilioAccountSid != null && !twilioAccountSid.isBlank() && twilioAuthToken != null && !twilioAuthToken.isBlank()) {
            sendTwilioSms(rawPhone, otpCode);
            return;
        }

        if (fast2smsApiKey != null && !fast2smsApiKey.isBlank()) {
            sendFast2SmsOtp(rawPhone, otpCode);
            return;
        }

        log.info("[SMS OTP - DEV MODE] Neither Twilio nor Fast2SMS configured. 4-digit OTP code for phone {}: {}", rawPhone, otpCode);
    }

    private void sendTwilioSms(String rawPhone, String otpCode) {
        try {
            String formattedPhone = rawPhone.trim();
            if (!formattedPhone.startsWith("+")) {
                String digits = formattedPhone.replaceAll("[^0-9]", "");
                if (digits.length() == 10) {
                    formattedPhone = "+91" + digits;
                } else {
                    formattedPhone = "+" + digits;
                }
            }
            String body = "Your Locality Connector 4-digit verification code is " + otpCode + ". Valid for 10 minutes.";
            String formData = "To=" + java.net.URLEncoder.encode(formattedPhone, java.nio.charset.StandardCharsets.UTF_8)
                    + "&From=" + java.net.URLEncoder.encode(twilioPhoneNumber != null ? twilioPhoneNumber.trim() : "", java.nio.charset.StandardCharsets.UTF_8)
                    + "&Body=" + java.net.URLEncoder.encode(body, java.nio.charset.StandardCharsets.UTF_8);

            String auth = twilioAccountSid.trim() + ":" + twilioAuthToken.trim();
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid.trim() + "/Messages.json"))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(java.time.Duration.ofSeconds(15))
                    .build();

            java.net.http.HttpResponse<String> response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Twilio SMS sent successfully to {}. Response: {}", formattedPhone, response.body());
            } else {
                log.warn("Twilio API error sending to {} (HTTP {}): {}", formattedPhone, response.statusCode(), response.body());
                log.info("[SMS OTP - FALLBACK] 4-digit OTP code for phone {}: {}", rawPhone, otpCode);
            }
        } catch (Exception e) {
            log.error("Failed to send SMS via Twilio to {}: {}", rawPhone, e.getMessage(), e);
            log.info("[SMS OTP - FALLBACK] 4-digit OTP code for phone {}: {}", rawPhone, otpCode);
        }
    }

    private void sendFast2SmsOtp(String rawPhone, String otpCode) {
        String digitsOnly = rawPhone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() > 10) {
            digitsOnly = digitsOnly.substring(digitsOnly.length() - 10);
        }

        if (fast2smsApiKey == null || fast2smsApiKey.trim().isEmpty()) {
            log.info("[SMS OTP - DEV MODE] FAST2SMS_API_KEY not set. 4-digit OTP code for phone {}: {}", rawPhone, otpCode);
            return;
        }

        try {
            String jsonPayload = """
                    {
                      "route": "q",
                      "message": "Your Locality Connector 4-digit verification code is %s. Valid for 10 minutes.",
                      "language": "english",
                      "flash": 0,
                      "numbers": "%s"
                    }
                    """.formatted(otpCode, digitsOnly);

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://www.fast2sms.com/dev/bulkV2"))
                    .header("authorization", fast2smsApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(java.time.Duration.ofSeconds(15))
                    .build();

            java.net.http.HttpResponse<String> response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Fast2SMS OTP sent successfully to {}. Response: {}", digitsOnly, response.body());
            } else {
                log.warn("Fast2SMS API error sending to {} (HTTP {}): {}", digitsOnly, response.statusCode(), response.body());
                log.info("[SMS OTP - FALLBACK] 4-digit OTP code for phone {}: {}", rawPhone, otpCode);
            }
        } catch (Exception e) {
            log.error("Failed to send SMS via Fast2SMS to {}: {}", rawPhone, e.getMessage(), e);
            log.info("[SMS OTP - FALLBACK] 4-digit OTP code for phone {}: {}", rawPhone, otpCode);
        }
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
