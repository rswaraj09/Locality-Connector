package com.example.localityconnector.controller;

import com.example.localityconnector.dto.FeedbackRequest;
import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.Feedback;
import com.example.localityconnector.model.User;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.FeedbackService;
import com.example.localityconnector.service.NotificationService;
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
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Customer feedback / ratings. Submitting requires an authenticated USER; the
 * reviewer identity is taken from the JWT, never from the request body.
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Submit, edit, delete and read business ratings")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final BusinessService businessService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Operation(summary = "Submit a rating for a business (USER only)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> submit(@Valid @RequestBody FeedbackRequest request) {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("User not logged in"));
        }

        Optional<Business> businessOpt = businessService.findById(request.getBusinessId());
        if (businessOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }

        String userName = SecurityUtils.getLoggedInEntityName();
        String userEmail = null;
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            userEmail = userOpt.get().getEmail();
            if (userName == null) {
                userName = userOpt.get().getName();
            }
        }

        Business business = businessOpt.get();
        Feedback saved = feedbackService.submitFeedback(
                business.getId(),
                business.getBusinessName(),
                userId,
                userName,
                userEmail,
                request.getRating(),
                request.getComment());

        // Notify business owner
        notificationService.notifyFeedbackReceived(
                business.getId(), business.getBusinessName(), userName, request.getRating());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Feedback submitted");
        data.put("feedbackId", saved.getId());
        data.put("rating", saved.getRating());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(data));
    }

    @Operation(summary = "Edit own review (USER) — only the original author can edit")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> update(@PathVariable String id,
                                                      @Valid @RequestBody FeedbackRequest request) {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Not logged in"));
        }
        Optional<Feedback> existing = feedbackService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Feedback not found"));
        }
        if (!userId.equals(existing.get().getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("You can only edit your own reviews"));
        }

        Feedback updated = feedbackService.updateFeedback(id, request.getRating(), request.getComment());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "message", "Review updated",
                "feedbackId", updated.getId(),
                "rating", updated.getRating())));
    }

    @Operation(summary = "Delete own review (USER) or any review (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable String id) {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Not logged in"));
        }
        Optional<Feedback> existing = feedbackService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Feedback not found"));
        }

        // Users can only delete their own, admins can delete any
        Feedback feedback = existing.get();
        boolean isOwner = entityId.equals(feedback.getUserId());
        boolean isAdmin = SecurityUtils.hasRole("ADMIN");
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("You can only delete your own reviews"));
        }

        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Review deleted")));
    }

    @Operation(summary = "Public: list feedback for a business")
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<Object>> getForBusiness(@PathVariable String businessId) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.getFeedbackByBusinessId(businessId)));
    }

    @Operation(summary = "Public: average rating for a business")
    @GetMapping("/business/{businessId}/rating")
    public ResponseEntity<ApiResponse<Object>> getAverageRating(@PathVariable String businessId) {
        double average = feedbackService.getAverageRating(businessId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("businessId", businessId);
        data.put("averageRating", Math.round(average * 100.0) / 100.0);
        data.put("reviewCount", feedbackService.getFeedbackByBusinessId(businessId).size());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Report a review for moderation")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/report")
    public ResponseEntity<ApiResponse<Object>> report(@PathVariable String id,
                                                      @RequestBody(required = false) Map<String, String> body) {
        String entityId = SecurityUtils.getLoggedInEntityId();
        if (entityId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Not logged in"));
        }
        String reason = body != null ? body.get("reason") : "Inappropriate content";
        Feedback reported = feedbackService.reportFeedback(id, reason);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Review reported successfully", "feedbackId", reported.getId())));
    }

    @Operation(summary = "Public: rating histogram for a business")
    @GetMapping("/business/{businessId}/histogram")
    public ResponseEntity<ApiResponse<Object>> getHistogram(@PathVariable String businessId) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.getRatingHistogram(businessId)));
    }
}
