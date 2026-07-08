package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.util.ApiResponse;
import com.example.localityconnector.util.GeolocationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Read-only reporting/browse endpoints over the businesses collection.
 *
 * <p>These endpoints now push filtering, sorting, pagination and counting down to
 * Firestore (orderBy / whereEqualTo / startAt-endAt / aggregation count) instead of
 * loading the entire collection and filtering in memory, which did not scale and was
 * the source of the P0 "full-collection scan" finding.</p>
 */
@RestController
@RequestMapping("/api/business-data")
@RequiredArgsConstructor
@Tag(name = "Business Data", description = "Aggregated, sortable, searchable business data")
public class BusinessDataController {

    /** Category buckets reported by {@code /stats} (matches {@code Constants.Categories}). */
    private static final List<String> KNOWN_CATEGORIES =
            List.of("food", "pharmacy", "clothing", "stationary", "hospital", "grocery");

    private final BusinessService businessService;

    @Operation(summary = "Paginated, sortable list of all businesses (server-side orderBy + offset/limit)")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Object>> getAllBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "businessName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 0);
        String sortField = normalizeSortField(sortBy);
        boolean descending = "desc".equalsIgnoreCase(sortDir);

        long total = businessService.countAll();
        List<Business> pageContent = businessService.findAllSorted(
                sortField, descending, safePage * safeSize, safeSize);

        int end = safePage * safeSize + pageContent.size();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("businesses", pageContent);
        response.put("currentPage", safePage);
        response.put("totalPages", (int) Math.ceil((double) total / safeSize));
        response.put("totalElements", total);
        response.put("size", safeSize);
        response.put("first", safePage == 0);
        response.put("last", end >= total);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** Only fields with a Firestore index/orderBy support are allowed; default to businessName. */
    private String normalizeSortField(String sortBy) {
        return switch (sortBy) {
            case "email" -> "email";
            case "category" -> "category";
            default -> "businessName";
        };
    }

    @Operation(summary = "Businesses located within a given Indian state (latitude range pushed to Firestore)")
    @GetMapping("/by-state")
    public ResponseEntity<ApiResponse<Object>> getBusinessesByState(@RequestParam String state) {
        double[] bounds = GeolocationUtils.latitudeBoundsForState(state);
        if (bounds == null) {
            // Unknown state: bound the scan to mainland India rather than the whole collection.
            bounds = GeolocationUtils.indiaLatitudeBounds();
        }
        // Firestore narrows by latitude; the precise longitude bounding box is applied in memory
        // on the (much smaller) candidate set.
        List<Business> stateBusinesses = businessService.findByLatitudeRange(bounds[0], bounds[1]).stream()
                .filter(b -> b.getLatitude() != null && b.getLongitude() != null
                        && GeolocationUtils.isInState(b.getLatitude(), b.getLongitude(), state))
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("state", state);
        response.put("count", stateBusinesses.size());
        response.put("businesses", stateBusinesses);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Active businesses in a category (whereEqualTo pushed to Firestore)")
    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<Object>> getBusinessesByCategory(@RequestParam String category) {
        List<Business> businesses = businessService.getBusinessesByCategory(category.toLowerCase());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("category", category);
        response.put("count", businesses.size());
        response.put("businesses", businesses);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Prefix search by business name (Firestore orderBy + startAt/endAt)")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Object>> searchBusinesses(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 0);

        long total = businessService.countByNamePrefix(query);
        List<Business> pageResults = businessService.searchByNamePrefix(
                query, safePage * safeSize, safeSize);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("businesses", pageResults);
        response.put("currentPage", safePage);
        response.put("totalPages", (int) Math.ceil((double) total / safeSize));
        response.put("totalElements", total);
        response.put("size", safeSize);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Aggregate statistics over all businesses (Firestore aggregation counts)")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getBusinessStats() {
        long total = businessService.countAll();
        long withCoordinates = businessService.countWithCoordinates();

        // Category distribution via server-side aggregation counts (no document reads).
        Map<String, Long> categoryStats = new LinkedHashMap<>();
        for (String category : KNOWN_CATEGORIES) {
            long count = businessService.countByCategory(category);
            if (count > 0) {
                categoryStats.put(category, count);
            }
        }

        // State distribution: classify only the geo-tagged candidates within India's latitude band.
        double[] india = GeolocationUtils.indiaLatitudeBounds();
        Map<String, Long> stateStats = businessService.findByLatitudeRange(india[0], india[1]).stream()
                .filter(b -> b.getLatitude() != null && b.getLongitude() != null)
                .collect(Collectors.groupingBy(
                        b -> GeolocationUtils.getIndianState(b.getLatitude(), b.getLongitude()),
                        TreeMap::new,
                        Collectors.counting()));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalBusinesses", total);
        stats.put("categoryDistribution", categoryStats);
        stats.put("stateDistribution", stateStats);
        stats.put("businessesWithCoordinates", withCoordinates);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @Operation(summary = "Export every business record (ADMIN only; locked down in SecurityConfig)")
    @GetMapping("/export")
    public ResponseEntity<ApiResponse<Object>> exportAllBusinesses() {
        return ResponseEntity.ok(ApiResponse.ok(businessService.findAllExport()));
    }
}
