package com.example.localityconnector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Sends transactional emails (verification, password reset). Uses Spring's
 * {@link JavaMailSender} backed by the configured SMTP provider.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${spring.mail.username:noreply@localityconnector.com}")
    private String fromEmail;

    @Value("${app.name:Locality Connector}")
    private String appName;

    public void sendVerificationEmail(String to, String token) {
        String link = baseUrl + "/api/auth/verify?token=" + token;
        String subject = appName + " — Verify Your Email";
        String body = """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 520px; margin: 0 auto; padding: 32px;">
                  <h2 style="color: #2563eb; margin-bottom: 8px;">%s</h2>
                  <p style="color: #374151; font-size: 15px;">Welcome! Please verify your email address to activate your account.</p>
                  <a href="%s"
                     style="display: inline-block; margin: 24px 0; padding: 12px 28px; background: #2563eb;
                            color: #fff; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 15px;">
                    Verify Email
                  </a>
                  <p style="color: #6b7280; font-size: 13px;">This link expires in 24 hours. If you didn't create an account, ignore this email.</p>
                  <p style="color: #9ca3af; font-size: 12px; margin-top: 32px; border-top: 1px solid #e5e7eb; padding-top: 16px;">
                    If the button doesn't work, copy and paste this URL into your browser:<br>
                    <a href="%s" style="color: #2563eb;">%s</a>
                  </p>
                </div>
                """.formatted(appName, link, link, link);

        sendHtmlEmail(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = baseUrl + "/reset-password?token=" + token;
        String subject = appName + " — Reset Your Password";
        String body = """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 520px; margin: 0 auto; padding: 32px;">
                  <h2 style="color: #2563eb; margin-bottom: 8px;">%s</h2>
                  <p style="color: #374151; font-size: 15px;">We received a request to reset your password.</p>
                  <a href="%s"
                     style="display: inline-block; margin: 24px 0; padding: 12px 28px; background: #2563eb;
                            color: #fff; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 15px;">
                    Reset Password
                  </a>
                  <p style="color: #6b7280; font-size: 13px;">This link expires in 1 hour. If you didn't request a reset, ignore this email.</p>
                  <p style="color: #9ca3af; font-size: 12px; margin-top: 32px; border-top: 1px solid #e5e7eb; padding-top: 16px;">
                    If the button doesn't work, copy and paste this URL into your browser:<br>
                    <a href="%s" style="color: #2563eb;">%s</a>
                  </p>
                </div>
                """.formatted(appName, link, link, link);

        sendHtmlEmail(to, subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
