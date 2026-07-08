package com.example.localityconnector.controller;

import com.example.localityconnector.dto.LocationBasedBusinessRequest;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.FavoriteService;
import com.example.localityconnector.service.FeedbackService;
import com.example.localityconnector.service.ItemService;
import com.example.localityconnector.service.UserService;
import com.example.localityconnector.util.ApiResponse;
import com.example.localityconnector.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * Public discovery endpoints used by the user-facing pages.
 */
@RestController
@RequestMapping("/api/user/dashboard")
@RequiredArgsConstructor
@Tag(name = "User Dashboard", description = "Browse and discover local businesses")
public class UserDashboardController {

    private final BusinessService businessService;
    private final ItemService itemService;
    private final FeedbackService feedbackService;
    private final FavoriteService favoriteService;
    private final UserService userService;

    @Operation(summary = "List all businesses")
    @GetMapping("/businesses")
    public ResponseEntity<ApiResponse<Object>> getBusinesses() {
        return ResponseEntity.ok(ApiResponse.ok(businessService.getAllBusinesses()));
    }

    @Operation(summary = "List active businesses in a category")
    @GetMapping("/businesses/category/{category}")
    public ResponseEntity<ApiResponse<Object>> getBusinessesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.ok(businessService.getBusinessesByCategory(category)));
    }

    @Operation(summary = "List items for a business")
    @GetMapping("/items/business/{businessId}")
    public ResponseEntity<ApiResponse<Object>> getItemsByBusiness(@PathVariable String businessId) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getItemsByBusinessId(businessId)));
    }

    @Operation(summary = "Find nearby businesses (JSON body)")
    @PostMapping("/businesses/nearby")
    public ResponseEntity<ApiResponse<Object>> getNearbyBusinesses(@RequestBody LocationBasedBusinessRequest request) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Latitude and longitude are required"));
        }
        return ResponseEntity.ok(ApiResponse.ok(businessService.getBusinessesWithinRadius(request)));
    }

    @Operation(summary = "Find nearby businesses (query params)")
    @GetMapping("/businesses/nearby")
    public ResponseEntity<ApiResponse<Object>> getNearbyBusinesses(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm,
            @RequestParam(required = false) String category) {
        LocationBasedBusinessRequest request = new LocationBasedBusinessRequest(latitude, longitude, radiusKm);
        if (category != null && !category.isEmpty()) {
            request.setCategory(category);
        }
        return ResponseEntity.ok(ApiResponse.ok(businessService.getBusinessesWithinRadius(request)));
    }

    @Operation(summary = "List reviews submitted by the logged-in user")
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<Object>> getUserReviews() {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Not logged in"));
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.getFeedbackByUserId(userId)));
    }

    @Operation(summary = "List saved favorites for the logged-in user")
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<Object>> getUserFavorites() {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Not logged in"));
        return ResponseEntity.ok(ApiResponse.ok(favoriteService.getUserFavorites(userId)));
    }

    @Operation(summary = "Get recent search history for the logged-in user")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Object>> getSearchHistory() {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Not logged in"));
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(userId).map(com.example.localityconnector.model.User::getSearchHistory).orElse(Collections.emptyList())));
    }

    @Operation(summary = "Add query to recent search history")
    @PostMapping("/history")
    public ResponseEntity<ApiResponse<Object>> addSearchHistory(@RequestParam String query) {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Not logged in"));
        userService.findById(userId).ifPresent(u -> {
            if (!u.getSearchHistory().contains(query)) {
                u.getSearchHistory().add(0, query);
                if (u.getSearchHistory().size() > 20) u.getSearchHistory().remove(u.getSearchHistory().size() - 1);
                u.prePersist();
                userService.updateUser(u);
            }
        });
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Search history updated")));
    }

    @Operation(summary = "Get notification preferences")
    @GetMapping("/notifications/preferences")
    public ResponseEntity<ApiResponse<Object>> getNotificationPreferences() {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Not logged in"));
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(userId).map(com.example.localityconnector.model.User::getNotificationPreferences).orElse(Collections.emptyMap())));
    }

    @Operation(summary = "Update notification preferences")
    @PutMapping("/notifications/preferences")
    public ResponseEntity<ApiResponse<Object>> updateNotificationPreferences(@RequestBody Map<String, Boolean> preferences) {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Not logged in"));
        userService.findById(userId).ifPresent(u -> {
            u.setNotificationPreferences(preferences);
            u.prePersist();
            userService.updateUser(u);
        });
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Notification preferences updated")));
    }
}
