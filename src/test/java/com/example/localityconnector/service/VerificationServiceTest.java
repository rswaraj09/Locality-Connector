package com.example.localityconnector.service;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.User;
import com.example.localityconnector.model.VerificationToken;
import com.example.localityconnector.repository.BusinessRepository;
import com.example.localityconnector.repository.UserRepository;
import com.example.localityconnector.repository.VerificationTokenRepository;
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
class VerificationServiceTest {

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationService verificationService;

    @Test
    void testCreateAndSendVerification() throws Exception {
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        String token = verificationService.createAndSendVerification("user123", "USER", "test@example.com");

        assertNotNull(token);
        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
        verify(emailService, times(1)).sendVerificationEmail(eq("test@example.com"), eq(token));
    }

    @Test
    void testVerifyUserSuccess() {
        VerificationToken token = new VerificationToken();
        token.setToken("valid-token");
        token.setEntityId("user123");
        token.setEntityType("USER");
        token.setUsed(false);
        token.setExpiresAt(new Date(System.currentTimeMillis() + 3600_000L));

        User user = new User();
        user.setId("user123");
        user.setEmailVerified(false);

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        boolean result = verificationService.verify("valid-token");

        assertTrue(result);
        assertTrue(token.isUsed());
        assertTrue(user.isEmailVerified());
        verify(tokenRepository, times(1)).save(token);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testVerifyBusinessSuccess() {
        VerificationToken token = new VerificationToken();
        token.setToken("valid-biz-token");
        token.setEntityId("biz123");
        token.setEntityType("BUSINESS");
        token.setUsed(false);
        token.setExpiresAt(new Date(System.currentTimeMillis() + 3600_000L));

        Business business = new Business();
        business.setId("biz123");
        business.setEmailVerified(false);

        when(tokenRepository.findByToken("valid-biz-token")).thenReturn(Optional.of(token));
        when(businessRepository.findById("biz123")).thenReturn(Optional.of(business));

        boolean result = verificationService.verify("valid-biz-token");

        assertTrue(result);
        assertTrue(token.isUsed());
        assertTrue(business.isEmailVerified());
        verify(tokenRepository, times(1)).save(token);
        verify(businessRepository, times(1)).save(business);
    }

    @Test
    void testVerifyExpiredOrUsedToken() {
        VerificationToken usedToken = new VerificationToken();
        usedToken.setUsed(true);
        usedToken.setExpiresAt(new Date(System.currentTimeMillis() + 3600_000L));

        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));
        assertFalse(verificationService.verify("used-token"));

        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setUsed(false);
        expiredToken.setExpiresAt(new Date(System.currentTimeMillis() - 3600_000L));

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));
        assertFalse(verificationService.verify("expired-token"));

        when(tokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());
        assertFalse(verificationService.verify("nonexistent"));
    }

    @Test
    void testCleanupExpiredTokens() {
        when(tokenRepository.deleteExpired()).thenReturn(5);
        verificationService.cleanupExpiredTokens();
        verify(tokenRepository, times(1)).deleteExpired();
    }
}
