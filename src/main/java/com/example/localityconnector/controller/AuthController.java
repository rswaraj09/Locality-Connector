package com.example.localityconnector.controller;

import com.example.localityconnector.dto.BusinessSignupRequest;
import com.example.localityconnector.dto.ForgotPasswordRequest;
import com.example.localityconnector.dto.LoginRequest;
import com.example.localityconnector.dto.ResetPasswordRequest;
import com.example.localityconnector.dto.UserSignupRequest;
import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.User;
import com.example.localityconnector.security.JwtUtil;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.JwtBlacklistService;
import com.example.localityconnector.service.LoginAttemptService;
import com.example.localityconnector.service.PasswordResetService;
import com.example.localityconnector.service.UserService;
import com.example.localityconnector.service.VerificationService;
import com.example.localityconnector.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Fully stateless authentication. Login issues a JWT that carries the entity id, display
 * name and roles; no {@link jakarta.servlet.http.HttpSession} is created. Repeated failures
 * are throttled by {@link LoginAttemptService}, and logout revokes the token via its jti.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Stateless JWT signup, login and logout")
public class AuthController {

    private final UserService userService;
    private final BusinessService businessService;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final JwtBlacklistService jwtBlacklistService;
    private final VerificationService verificationService;
    private final PasswordResetService passwordResetService;

    @Value("${app.admin.emails:}")
    private String adminEmailsRaw;

    /** Parsed once at startup — no per-request CSV parsing. */
    private Set<String> adminEmailsCache = Collections.emptySet();

    @PostConstruct
    void initAdminEmails() {
        Set<String> set = new HashSet<>();
        if (adminEmailsRaw != null) {
            for (String email : adminEmailsRaw.split(",")) {
                String trimmed = email.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    set.add(trimmed);
                }
            }
        }
        this.adminEmailsCache = Collections.unmodifiableSet(set);
    }

    private boolean isAdmin(String email) {
        return email != null && adminEmailsCache.contains(email.toLowerCase());
    }

    @Operation(summary = "Register a new user account")
    @PostMapping("/user/signup")
    public ResponseEntity<ApiResponse<Object>> userSignup(@Valid @RequestBody UserSignupRequest request) {
        User user = userService.signup(request);
        // Send verification email
        verificationService.createAndSendVerification(user.getId(), "USER", user.getEmail());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "User registered successfully. Please check your email to verify your account.");
        data.put("userId", user.getId());
        data.put("email", user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(data));
    }

    @Operation(summary = "Register a new business account")
    @PostMapping("/business/signup")
    public ResponseEntity<ApiResponse<Object>> businessSignup(@Valid @RequestBody BusinessSignupRequest request) {
        Business business = businessService.signup(request);
        // Send verification email
        verificationService.createAndSendVerification(business.getId(), "BUSINESS", business.getEmail());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Business registered successfully. Please check your email to verify your account.");
        data.put("businessId", business.getId());
        data.put("businessName", business.getBusinessName());
        data.put("email", business.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(data));
    }

    @Operation(summary = "User login; returns a JWT")
    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<Object>> userLogin(@Valid @RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        ResponseEntity<ApiResponse<Object>> locked = lockoutResponse(email);
        if (locked != null) {
            return locked;
        }

        Optional<User> userOpt = userService.login(email, loginRequest.getPassword());
        if (userOpt.isEmpty()) {
            loginAttemptService.loginFailed(email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Invalid email or password"));
        }
        loginAttemptService.loginSucceeded(email);

        User user = userOpt.get();
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        if (isAdmin(email)) {
            roles.add("ADMIN");
        }
        String token = jwtUtil.generateToken(user.getEmail(), roles, user.getId(), user.getName());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Login successful");
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("roles", roles);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Business login; returns a JWT")
    @PostMapping("/business/login")
    public ResponseEntity<ApiResponse<Object>> businessLogin(@Valid @RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        ResponseEntity<ApiResponse<Object>> locked = lockoutResponse(email);
        if (locked != null) {
            return locked;
        }

        Optional<Business> businessOpt = businessService.login(email, loginRequest.getPassword());
        if (businessOpt.isEmpty()) {
            loginAttemptService.loginFailed(email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Invalid email or password"));
        }
        loginAttemptService.loginSucceeded(email);

        Business business = businessOpt.get();
        List<String> roles = new ArrayList<>();
        roles.add("BUSINESS");
        if (isAdmin(email)) {
            roles.add("ADMIN");
        }
        String token = jwtUtil.generateToken(business.getEmail(), roles, business.getId(), business.getBusinessName());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Login successful");
        data.put("token", token);
        data.put("businessId", business.getId());
        data.put("businessName", business.getBusinessName());
        data.put("email", business.getEmail());
        data.put("category", business.getCategory());
        data.put("roles", roles);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Logout; revokes the presented JWT until it expires")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String jti = jwtUtil.extractJti(token);
                Date expiration = jwtUtil.extractExpiration(token);
                jwtBlacklistService.blacklist(jti, expiration);
            } catch (Exception ignored) {
                // A malformed/expired token needs no revocation.
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Logged out successfully")));
    }

    @Operation(summary = "Refresh JWT token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refreshToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String jti = jwtUtil.extractJti(token);
                if (jwtBlacklistService.isBlacklisted(jti)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Token is blacklisted"));
                }
                String email = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token);
                String entityId = jwtUtil.extractEntityId(token);
                String entityName = jwtUtil.extractEntityName(token);

                // Blacklist old token
                jwtBlacklistService.blacklist(jti, jwtUtil.extractExpiration(token));

                // Generate new token
                String newToken = jwtUtil.generateToken(email, roles, entityId, entityName);
                return ResponseEntity.ok(ApiResponse.ok(Map.of("token", newToken, "message", "Token refreshed successfully")));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Invalid or expired token"));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Authorization header missing"));
    }

    @Operation(summary = "Resolve the logged-in business display name from the bearer token")
    @GetMapping("/session/business-name")
    public ResponseEntity<ApiResponse<Object>> sessionBusinessName(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String name = jwtUtil.extractEntityName(token);
                if (name != null) {
                    return ResponseEntity.ok(ApiResponse.ok(Map.of("businessName", name)));
                }
            } catch (Exception ignored) {
                // Fall through to unauthorized.
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Business not logged in"));
    }

    // --- Email Verification ---

    @Operation(summary = "Verify email address using the token from the verification email")
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@RequestParam String token) {
        boolean verified = verificationService.verify(token);
        if (verified) {
            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Email verified successfully")));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("Invalid or expired verification token"));
    }

    @Operation(summary = "Resend verification email")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Object>> resendVerification(@RequestParam String email) {
        // Look up user or business
        var userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            verificationService.createAndSendVerification(userOpt.get().getId(), "USER", email);
        } else {
            var businessOpt = businessService.findByEmail(email);
            if (businessOpt.isPresent()) {
                verificationService.createAndSendVerification(businessOpt.get().getId(), "BUSINESS", email);
            }
        }
        // Always return success to prevent email enumeration
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message",
                "If an account exists with this email, a verification link has been sent.")));
    }

    // --- Password Reset ---

    @Operation(summary = "Request a password reset email")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message",
                "If an account exists with this email, a password reset link has been sent.")));
    }

    @Operation(summary = "Reset password using token")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean reset = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (reset) {
            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Password reset successfully")));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("Invalid or expired reset token"));
    }

    /** @return a 429 response when the account is locked, otherwise {@code null}. */
    private ResponseEntity<ApiResponse<Object>> lockoutResponse(String email) {
        if (loginAttemptService.isLocked(email)) {
            long retryAfter = loginAttemptService.getRetryAfterSeconds(email);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter))
                    .body(ApiResponse.fail("Too many failed login attempts. Try again in " + retryAfter + " seconds."));
        }
        return null;
    }
}
