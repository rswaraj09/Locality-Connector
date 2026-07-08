package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.Feedback;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.GooglePlacesService;
import com.example.localityconnector.service.FeedbackService;
import com.example.localityconnector.service.ItemService;
import com.example.localityconnector.service.NotificationService;
import com.example.localityconnector.service.UserService;
import com.example.localityconnector.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Administrative operations. Access is restricted to ADMIN via SecurityConfig
 * ({@code /api/admin/**}); admins are identified by the configured
 * {@code app.admin.emails} list at login time.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative moderation and management")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final BusinessService businessService;
    private final UserService userService;
    private final ItemService itemService;
    private final FeedbackService feedbackService;
    private final GooglePlacesService googlePlacesService;
    private final NotificationService notificationService;

    @Operation(summary = "List businesses awaiting verification")
    @GetMapping("/businesses/unverified")
    public ResponseEntity<ApiResponse<Object>> unverifiedBusinesses() {
        List<Business> unverified = businessService.getUnverifiedBusinesses();
        return ResponseEntity.ok(ApiResponse.ok(unverified));
    }

    @Operation(summary = "List all businesses")
    @GetMapping("/businesses")
    public ResponseEntity<ApiResponse<Object>> allBusinesses() {
        return ResponseEntity.ok(ApiResponse.ok(businessService.getAllBusinesses()));
    }

    @Operation(summary = "List all users")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Object>> allUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @Operation(summary = "Get platform statistics")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> platformStatistics() {
        List<Business> businesses = businessService.getAllBusinesses();
        long totalBusinesses = businesses.size();
        long unverifiedBusinesses = businesses.stream().filter(b -> !b.isVerified()).count();
        long activeBusinesses = businesses.stream().filter(Business::isActive).count();
        long totalUsers = userService.getAllUsers().size();
        List<Feedback> feedbackList = feedbackService.getAllFeedback();
        long totalReviews = feedbackList.size();
        long flaggedReviews = feedbackList.stream().filter(Feedback::isFlagged).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalBusinesses", totalBusinesses);
        stats.put("unverifiedBusinesses", unverifiedBusinesses);
        stats.put("activeBusinesses", activeBusinesses);
        stats.put("totalUsers", totalUsers);
        stats.put("totalReviews", totalReviews);
        stats.put("flaggedReviews", flaggedReviews);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @Operation(summary = "Suspend (deactivate) a user")
    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<Object>> suspendUser(@PathVariable String id) {
        if (userService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("User not found"));
        }
        com.example.localityconnector.model.User suspended = userService.setActive(id, false);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "User suspended", "userId", suspended.getId(), "active", suspended.isActive())));
    }

    @Operation(summary = "Reactivate a user")
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<Object>> activateUser(@PathVariable String id) {
        if (userService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("User not found"));
        }
        com.example.localityconnector.model.User activated = userService.setActive(id, true);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "User activated", "userId", activated.getId(), "active", activated.isActive())));
    }

    @Operation(summary = "Delete a user")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable String id) {
        if (userService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("User not found"));
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "User deleted", "userId", id)));
    }

    @Operation(summary = "Verify a business")
    @PutMapping("/businesses/{id}/verify")
    public ResponseEntity<ApiResponse<Object>> verify(@PathVariable String id) {
        Optional<Business> existing = businessService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }
        Business verified = businessService.verifyBusiness(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Business verified");
        data.put("businessId", verified.getId());
        data.put("verified", verified.isVerified());
        notificationService.notifyBusinessVerified(verified.getId(), verified.getBusinessName());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Suspend (deactivate) a business")
    @PutMapping("/businesses/{id}/suspend")
    public ResponseEntity<ApiResponse<Object>> suspend(@PathVariable String id) {
        Optional<Business> existing = businessService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }
        Business suspended = businessService.setActive(id, false);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Business suspended");
        data.put("businessId", suspended.getId());
        data.put("active", suspended.isActive());
        notificationService.notifyBusinessSuspended(suspended.getId(), suspended.getBusinessName());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Reactivate a business")
    @PutMapping("/businesses/{id}/activate")
    public ResponseEntity<ApiResponse<Object>> activate(@PathVariable String id) {
        Optional<Business> existing = businessService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }
        Business activated = businessService.setActive(id, true);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Business activated");
        data.put("businessId", activated.getId());
        data.put("active", activated.isActive());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Delete a business and cascade its items and feedback")
    @DeleteMapping("/businesses/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteBusiness(@PathVariable String id) {
        Optional<Business> existing = businessService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }
        int itemsRemoved = itemService.deleteItemsByBusinessId(id);
        int feedbackRemoved = feedbackService.deleteFeedbackByBusinessId(id);
        businessService.deleteBusiness(id);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Business deleted");
        data.put("businessId", id);
        data.put("itemsRemoved", itemsRemoved);
        data.put("feedbackRemoved", feedbackRemoved);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Seed businesses from Google Places near a coordinate")
    @PostMapping("/seed-from-places")
    public ResponseEntity<ApiResponse<Object>> seedFromPlaces(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") int radius,
            @RequestParam(defaultValue = "restaurant,pharmacy,clothing_store,hospital,store") String types) {
        String[] typeArray = types.split(",");
        List<Business> found = googlePlacesService.searchNearbyBusinesses(latitude, longitude, radius, typeArray);

        int seeded = 0;
        int skipped = 0;
        for (Business business : found) {
            if (business.getEmail() != null && businessService.existsByEmail(business.getEmail())) {
                skipped++;
                continue;
            }
            businessService.saveBusiness(business);
            seeded++;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Seeded businesses from Google Places");
        data.put("found", found.size());
        data.put("seeded", seeded);
        data.put("skipped", skipped);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // --- Review Moderation ---

    @Operation(summary = "List flagged reviews")
    @GetMapping("/feedback/flagged")
    public ResponseEntity<ApiResponse<Object>> flaggedFeedback() {
        List<Feedback> all = feedbackService.getAllFeedback();
        List<Feedback> flagged = all.stream().filter(Feedback::isFlagged).toList();
        return ResponseEntity.ok(ApiResponse.ok(flagged));
    }

    @Operation(summary = "Flag a review for moderation")
    @PutMapping("/feedback/{id}/flag")
    public ResponseEntity<ApiResponse<Object>> flagFeedback(@PathVariable String id,
                                                            @RequestParam(required = false) String note) {
        Optional<Feedback> feedbackOpt = feedbackService.findById(id);
        if (feedbackOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Feedback not found"));
        }
        Feedback feedback = feedbackOpt.get();
        feedback.setFlagged(true);
        if (note != null) feedback.setModerationNote(note);
        feedback.prePersist();
        feedbackService.createFeedback(feedback);
        // Notify the author
        if (feedback.getUserId() != null) {
            notificationService.notifyReviewFlagged(feedback.getUserId(), id);
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Review flagged")));
    }

    @Operation(summary = "Remove a flagged review")
    @DeleteMapping("/feedback/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteFeedback(@PathVariable String id) {
        Optional<Feedback> feedbackOpt = feedbackService.findById(id);
        if (feedbackOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Feedback not found"));
        }
        Feedback feedback = feedbackOpt.get();
        feedbackService.deleteFeedback(id);
        // Notify the author
        if (feedback.getUserId() != null) {
            notificationService.notifyReviewRemoved(feedback.getUserId(), id);
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Review removed")));
    }
}
