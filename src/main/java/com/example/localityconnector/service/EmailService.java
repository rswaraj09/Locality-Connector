package com.example.localityconnector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails (verification, password reset, 4-digit OTP).
 * Delegates sending to ResendEmailService REST API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ResendEmailService resendEmailService;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${app.name:Locality Connector}")
    private String appName;

    public void sendWelcomeSuccessEmail(String to, String name, String accountType) {
        String loginPath = "BUSINESS".equalsIgnoreCase(accountType) ? "/business/login" : "/user/login";
        String link = baseUrl + loginPath;
        String subject = appName + " — Account Created Successfully!";
        String body = """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 520px; margin: 0 auto; padding: 32px; background: #0d1210; color: #f6f1e4; border-radius: 16px; border: 1px solid rgba(246,241,228,0.2);">
                  <h2 style="color: #ff6b35; margin-bottom: 8px;">Welcome to %s! 🎉</h2>
                  <p style="color: #f6f1e4; font-size: 16px; margin-top: 16px;">Hello <strong>%s</strong>,</p>
                  <p style="color: rgba(246,241,228,0.85); font-size: 15px; line-height: 1.6;">Your %s account has been <strong>successfully created and activated</strong>.</p>
                  <p style="color: rgba(246,241,228,0.85); font-size: 15px; line-height: 1.6;">You can now log in anytime to discover local businesses and connect with your neighborhood.</p>
                  <div style="margin: 28px 0; text-align: center;">
                    <a href="%s"
                       style="display: inline-block; padding: 14px 32px; background: #ff6b35; color: #ffffff; text-decoration: none; border-radius: 8px; font-weight: 700; font-size: 15px;">
                      Log In to Your Account
                    </a>
                  </div>
                  <p style="color: rgba(246,241,228,0.5); font-size: 13px; border-top: 1px solid rgba(246,241,228,0.1); padding-top: 16px;">Thank you for joining %s!</p>
                </div>
                """.formatted(appName, name != null && !name.isBlank() ? name : "there", accountType.toLowerCase(), link, appName);

        sendHtmlEmail(to, subject, body);
    }

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
                </div>
                """.formatted(appName, link);

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
                </div>
                """.formatted(appName, link);

        sendHtmlEmail(to, subject, body);
    }

    public void sendRegistrationOtpEmail(String to, String otpCode) {
        String subject = appName + " — Registration 4-Digit OTP Verification";
        String body = """
                <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 520px; margin: 0 auto; padding: 32px; background: #0f0a14; color: #ffffff; border-radius: 16px;">
                  <h2 style="color: #f0b429; margin-bottom: 8px;">%s</h2>
                  <p style="color: #e2d9f3; font-size: 15px;">Your 4-digit registration verification code is:</p>
                  <div style="margin: 24px 0; padding: 16px 32px; background: rgba(240, 180, 41, 0.15); border: 2px dashed #f0b429; border-radius: 12px; font-size: 36px; font-weight: 800; letter-spacing: 8px; color: #ffd580; text-align: center;">
                    %s
                  </div>
                  <p style="color: #a095bd; font-size: 13px;">This OTP expires in 10 minutes. Do not share this code with anyone.</p>
                </div>
                """.formatted(appName, otpCode);

        sendHtmlEmail(to, subject, body);
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        resendEmailService.sendEmail(to, subject, htmlBody);
    }
}

