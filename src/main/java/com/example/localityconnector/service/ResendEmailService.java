package com.example.localityconnector.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Transactional email service powered by Resend.com REST API.
 * Uses Java 11+ HttpClient for zero external dependency overhead.
 * If {@code RESEND_API_KEY} is missing, logs email payload to console.
 */
@Slf4j
@Service
public class ResendEmailService {

    @Value("${resend.api.key:${RESEND_API_KEY:}}")
    private String apiKey;

    @Value("${resend.from.email:${RESEND_FROM_EMAIL:onboarding@resend.dev}}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean sendEmail(String to, String subject, String htmlContent) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.info("[DEV MODE - RESEND_API_KEY not set] Simulated Email to {}: subject='{}'", to, subject);
            log.debug("Email body:\n{}", htmlContent);
            return true;
        }

        try {
            String jsonPayload = """
                    {
                      "from": "%s",
                      "to": ["%s"],
                      "subject": "%s",
                      "html": "%s"
                    }
                    """.formatted(
                        escapeJson(fromEmail),
                        escapeJson(to),
                        escapeJson(subject),
                        escapeJson(htmlContent)
                    );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Resend email sent successfully to {}. Response: {}", to, response.body());
                return true;
            } else {
                log.warn("Resend API error sending to {} (HTTP {}): {}", to, response.statusCode(), response.body());
                log.info("[CONSOLE FALLBACK] Simulated Email to {}: subject='{}'\n{}", to, subject, htmlContent);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to send email via Resend API to {}: {}", to, e.getMessage(), e);
            log.info("[CONSOLE FALLBACK] Simulated Email to {}: subject='{}'\n{}", to, subject, htmlContent);
            return false;
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
