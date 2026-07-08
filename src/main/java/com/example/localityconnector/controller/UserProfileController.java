package com.example.localityconnector.controller;

import com.example.localityconnector.dto.ChangePasswordRequest;
import com.example.localityconnector.dto.UpdateUserRequest;
import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.User;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.UserService;
import com.example.localityconnector.util.ApiResponse;
import com.example.localityconnector.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User profile management — view, update, and change password.
 * Works for both USER and BUSINESS roles.
 */
@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Profile management for users and businesses")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserService userService;
    private final BusinessService businessService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Get the logged-in user's or business's profile")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getProfile() {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }

        // Try user first, then business
        Optional<User> userOpt = userService.findById(entityId);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(userOpt.get()));
        }

        Optional<Business> businessOpt = businessService.findById(entityId);
        if (businessOpt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.ok(businessOpt.get()));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("Profile not found"));
    }

    @Operation(summary = "Update user profile (name, address, phone)")
    @PutMapping
    public ResponseEntity<ApiResponse<Object>> updateProfile(
            @Valid @RequestBody UpdateUserRequest request) {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }

        Optional<User> userOpt = userService.findById(entityId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(request.getName());
            if (request.getAddress() != null) user.setAddress(request.getAddress());
            if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
            user.prePersist();
            userService.updateUser(user);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Profile updated")));
        }

        // For business owners, update name and phone
        Optional<Business> businessOpt = businessService.findById(entityId);
        if (businessOpt.isPresent()) {
            Business business = businessOpt.get();
            business.setOwnerName(request.getName());
            if (request.getAddress() != null) business.setAddress(request.getAddress());
            if (request.getPhoneNumber() != null) business.setPhoneNumber(request.getPhoneNumber());
            business.prePersist();
            businessService.saveBusiness(business);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Profile updated")));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("Profile not found"));
    }

    @Operation(summary = "Change password")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }

        // Try user
        Optional<User> userOpt = userService.findById(entityId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("Current password is incorrect"));
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.prePersist();
            userService.updateUser(user);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Password changed successfully")));
        }

        // Try business
        Optional<Business> businessOpt = businessService.findById(entityId);
        if (businessOpt.isPresent()) {
            Business business = businessOpt.get();
            if (!passwordEncoder.matches(request.getCurrentPassword(), business.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("Current password is incorrect"));
            }
            business.setPassword(passwordEncoder.encode(request.getNewPassword()));
            business.prePersist();
            businessService.saveBusiness(business);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Password changed successfully")));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("Profile not found"));
    }
}
