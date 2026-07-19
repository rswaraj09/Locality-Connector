package com.example.localityconnector.service;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.PasswordResetToken;
import com.example.localityconnector.model.User;
import com.example.localityconnector.repository.BusinessRepository;
import com.example.localityconnector.repository.PasswordResetTokenRepository;
import com.example.localityconnector.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void testRequestResetUser() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        passwordResetService.requestReset("user@example.com");

        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendPasswordResetEmail(eq("user@example.com"), anyString());
    }

    @Test
    void testRequestResetBusiness() throws Exception {
        when(userRepository.findByEmail("biz@example.com")).thenReturn(Optional.empty());
        Business biz = new Business();
        biz.setEmail("biz@example.com");
        when(businessRepository.findByEmail("biz@example.com")).thenReturn(Optional.of(biz));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        passwordResetService.requestReset("biz@example.com");

        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendPasswordResetEmail(eq("biz@example.com"), anyString());
    }

    @Test
    void testRequestResetUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(businessRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestReset("unknown@example.com");

        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void testResetPasswordUserSuccess() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset-token");
        token.setEmail("user@example.com");
        token.setEntityType("USER");
        token.setUsed(false);
        token.setExpiresAt(new Date(System.currentTimeMillis() + 3600_000L));

        User user = new User();
        user.setEmail("user@example.com");

        when(tokenRepository.findByToken("reset-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass123")).thenReturn("hashedPass");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        boolean result = passwordResetService.resetPassword("reset-token", "newPass123");

        assertTrue(result);
        assertTrue(token.isUsed());
        assertEquals("hashedPass", user.getPassword());
        verify(tokenRepository, times(1)).save(token);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testResetPasswordBusinessSuccess() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset-biz-token");
        token.setEmail("biz@example.com");
        token.setEntityType("BUSINESS");
        token.setUsed(false);
        token.setExpiresAt(new Date(System.currentTimeMillis() + 3600_000L));

        Business biz = new Business();
        biz.setEmail("biz@example.com");

        when(tokenRepository.findByToken("reset-biz-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass123")).thenReturn("hashedPassBiz");
        when(businessRepository.findByEmail("biz@example.com")).thenReturn(Optional.of(biz));

        boolean result = passwordResetService.resetPassword("reset-biz-token", "newPass123");

        assertTrue(result);
        assertTrue(token.isUsed());
        assertEquals("hashedPassBiz", biz.getPassword());
        verify(tokenRepository, times(1)).save(token);
        verify(businessRepository, times(1)).save(biz);
    }

    @Test
    void testResetPasswordExpiredOrInvalid() {
        when(tokenRepository.findByToken("invalid")).thenReturn(Optional.empty());
        assertFalse(passwordResetService.resetPassword("invalid", "pass"));

        PasswordResetToken expired = new PasswordResetToken();
        expired.setUsed(false);
        expired.setExpiresAt(new Date(System.currentTimeMillis() - 3600_000L));
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));
        assertFalse(passwordResetService.resetPassword("expired", "pass"));
    }

    @Test
    void testCleanupExpiredTokens() {
        when(tokenRepository.deleteExpired()).thenReturn(3);
        passwordResetService.cleanupExpiredTokens();
        verify(tokenRepository, times(1)).deleteExpired();
    }
}
