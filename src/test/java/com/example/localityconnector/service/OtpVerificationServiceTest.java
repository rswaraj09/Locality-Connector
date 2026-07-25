package com.example.localityconnector.service;

import com.example.localityconnector.model.OtpToken;
import com.example.localityconnector.repository.OtpTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpVerificationServiceTest {

    @Mock
    OtpTokenRepository otpTokenRepository;

    @Mock
    EmailService emailService;

    @InjectMocks
    OtpVerificationService otpVerificationService;

    @BeforeEach
    void setUp() {
        lenient().when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void sendOtp_generates4DigitCode() {
        String code = otpVerificationService.sendOtp("test@example.com", "EMAIL");
        assertNotNull(code);
        assertEquals(4, code.length());
        assertTrue(Integer.parseInt(code) >= 1000 && Integer.parseInt(code) <= 9999);
        verify(emailService).sendRegistrationOtpEmail(eq("test@example.com"), eq(code));
    }

    @Test
    void verifyOtp_success() {
        OtpToken token = new OtpToken();
        token.setTarget("user@example.com");
        token.setOtpCode("1234");
        token.setExpiresAt(new Date(System.currentTimeMillis() + 600000));
        when(otpTokenRepository.findTopByTargetAndOtpCodeOrderByCreatedAtDesc("user@example.com", "1234"))
                .thenReturn(Optional.of(token));

        boolean verified = otpVerificationService.verifyOtp("user@example.com", "1234");
        assertTrue(verified);
        assertTrue(token.isVerified());
    }

    @Test
    void verifyOtp_expiredToken() {
        OtpToken token = new OtpToken();
        token.setTarget("user@example.com");
        token.setOtpCode("1234");
        token.setExpiresAt(new Date(System.currentTimeMillis() - 1000));
        when(otpTokenRepository.findTopByTargetAndOtpCodeOrderByCreatedAtDesc("user@example.com", "1234"))
                .thenReturn(Optional.of(token));

        boolean verified = otpVerificationService.verifyOtp("user@example.com", "1234");
        assertFalse(verified);
    }
}
