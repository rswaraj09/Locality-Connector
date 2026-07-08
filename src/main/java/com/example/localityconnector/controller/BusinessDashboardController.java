package com.example.localityconnector.controller;

import com.example.localityconnector.dto.CustomerSummaryDTO;
import com.example.localityconnector.dto.SaveLocationRequest;
import com.example.localityconnector.dto.UpdateBusinessRequest;
import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.Feedback;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.FavoriteService;
import com.example.localityconnector.service.FeedbackService;
import com.example.localityconnector.service.ItemService;
import com.example.localityconnector.service.StorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business-owner dashboard. The owning business is resolved from the JWT principal, so a
 * business can only ever see its own feedback and customers.
 */
@RestController
@RequestMapping("/api/business/dashboard")
@RequiredArgsConstructor
@Tag(name = "Business Dashboard", description = "Owner-only feedback, customers, analytics and profile")
@SecurityRequirement(name = "bearerAuth")
public class BusinessDashboardController {

    private final FeedbackService feedbackService;
    private final BusinessService businessService;
    private final ItemService itemService;
    private final StorageService storageService;
    private final FavoriteService favoriteService;

    @Operation(summary = "Feedback received by the logged-in business")
    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<Object>> getFeedback() {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.getFeedbackByBusinessId(businessId)));
    }

    @Operation(summary = "Distinct customers who reviewed the logged-in business")
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<Object>> getCustomers() {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        // Derive customers from this business's own feedback only - never expose the full user table.
        Map<String, CustomerSummaryDTO> unique = new LinkedHashMap<>();
        for (Feedback feedback : feedbackService.getFeedbackByBusinessId(businessId)) {
            String key = feedback.getUserId() != null ? feedback.getUserId() : feedback.getUserEmail();
            if (key == null) {
                continue;
            }
            unique.putIfAbsent(key, new CustomerSummaryDTO(feedback.getUserName(), feedback.getUserEmail()));
        }
        return ResponseEntity.ok(ApiResponse.ok(new ArrayList<>(unique.values())));
    }

    @Operation(summary = "Dashboard analytics for the logged-in business")
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Object>> getAnalytics() {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        Optional<Business> businessOpt = businessService.findById(businessId);
        if (businessOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }

        Business business = businessOpt.get();
        List<Feedback> feedbackList = feedbackService.getFeedbackByBusinessId(businessId);
        int itemCount = itemService.getItemsByBusinessId(businessId).size();

        // Rating distribution
        Map<Integer, Long> ratingDistribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            ratingDistribution.put(star, feedbackList.stream()
                    .filter(f -> f.getRating() != null && f.getRating() == star)
                    .count());
        }

        int favCount = favoriteService.getBusinessFavorites(businessId).size();

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("businessName", business.getBusinessName());
        analytics.put("averageRating", business.getAverageRating());
        analytics.put("totalReviews", feedbackList.size());
        analytics.put("ratingDistribution", ratingDistribution);
        analytics.put("totalItems", itemCount);
        analytics.put("verified", business.isVerified());
        analytics.put("active", business.isActive());
        analytics.put("logoUrl", business.getLogoUrl());
        analytics.put("viewsCount", business.getViewsCount());
        analytics.put("clicksCount", business.getClicksCount());
        analytics.put("favoritesCount", Math.max(favCount, business.getFavoritesCount()));
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    @Operation(summary = "Get the logged-in business profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Object>> getProfile() {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        Optional<Business> existingOpt = businessService.findById(businessId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(existingOpt.get()));
    }

    @Operation(summary = "Update the logged-in business profile")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Object>> updateProfile(@Valid @RequestBody UpdateBusinessRequest request) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        Optional<Business> existingOpt = businessService.findById(businessId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }
        Business current = existingOpt.get();

        Business details = new Business();
        details.setBusinessName(request.getBusinessName());
        details.setOwnerName(current.getOwnerName());
        details.setCategory(request.getBusinessType());
        details.setDescription(request.getBusinessDescription());
        details.setAddress(request.getBusinessAddress());
        details.setPhoneNumber(request.getContactNumber());
        details.setBusinessLicense(current.getBusinessLicense());

        Business saved = businessService.updateBusiness(businessId, details);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Business profile updated successfully");
        data.put("businessId", saved.getId());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Update the logged-in business's location (replaces the legacy /saveLocation)")
    @PutMapping("/location")
    public ResponseEntity<ApiResponse<Object>> updateLocation(@Valid @RequestBody SaveLocationRequest request) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        Optional<Business> existingOpt = businessService.findById(businessId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }
        Business saved = businessService.updateLocation(businessId,
                request.getBusinessLatitude(), request.getBusinessLongitude());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Location saved successfully");
        data.put("businessId", saved.getId());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Upload business logo")
    @PostMapping("/logo")
    public ResponseEntity<ApiResponse<Object>> uploadLogo(@RequestParam("file") MultipartFile file) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        try {
            Optional<Business> businessOpt = businessService.findById(businessId);
            if (businessOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
            }
            Business business = businessOpt.get();

            // Delete old logo if exists
            if (business.getLogoUrl() != null) {
                storageService.deleteByUrl(business.getLogoUrl());
            }

            String url = storageService.uploadImage(file, "logos");
            business.setLogoUrl(url);
            business.prePersist();
            businessService.saveBusiness(business);

            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Logo uploaded", "logoUrl", url)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("Failed to upload logo"));
        }
    }

    @Operation(summary = "Upload storefront image")
    @PostMapping("/storefront")
    public ResponseEntity<ApiResponse<Object>> uploadStorefront(@RequestParam("file") MultipartFile file) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        try {
            Optional<Business> businessOpt = businessService.findById(businessId);
            if (businessOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
            }
            Business business = businessOpt.get();

            if (business.getStorefrontUrl() != null) {
                storageService.deleteByUrl(business.getStorefrontUrl());
            }

            String url = storageService.uploadImage(file, "storefronts");
            business.setStorefrontUrl(url);
            business.prePersist();
            businessService.saveBusiness(business);

            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Storefront uploaded", "storefrontUrl", url)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("Failed to upload storefront image"));
        }
    }

    @Operation(summary = "Upload business photos (max 10 allowed)")
    @PostMapping("/photos")
    public ResponseEntity<ApiResponse<Object>> uploadPhotos(@RequestParam("files") MultipartFile[] files) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        try {
            Optional<Business> businessOpt = businessService.findById(businessId);
            if (businessOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
            }
            Business business = businessOpt.get();
            if (business.getPhotoUrls() == null) {
                business.setPhotoUrls(new ArrayList<>());
            }
            if (business.getPhotoUrls().size() + files.length > 10) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail("Maximum 10 business photos allowed"));
            }

            List<String> uploadedUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                String url = storageService.uploadImage(file, "business_photos");
                uploadedUrls.add(url);
                business.getPhotoUrls().add(url);
            }
            business.prePersist();
            businessService.saveBusiness(business);

            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Photos uploaded successfully", "photoUrls", business.getPhotoUrls())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("Failed to upload photos"));
        }
    }

    @Operation(summary = "Delete a business photo")
    @DeleteMapping("/photos")
    public ResponseEntity<ApiResponse<Object>> deletePhoto(@RequestParam("url") String url) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        try {
            Optional<Business> businessOpt = businessService.findById(businessId);
            if (businessOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
            }
            Business business = businessOpt.get();
            if (business.getPhotoUrls() != null && business.getPhotoUrls().contains(url)) {
                storageService.deleteByUrl(url);
                business.getPhotoUrls().remove(url);
                business.prePersist();
                businessService.saveBusiness(business);
                return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Photo deleted", "photoUrls", business.getPhotoUrls())));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Photo URL not found in business profile"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("Failed to delete photo"));
        }
    }

    private ResponseEntity<ApiResponse<Object>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Business not logged in"));
    }
}
