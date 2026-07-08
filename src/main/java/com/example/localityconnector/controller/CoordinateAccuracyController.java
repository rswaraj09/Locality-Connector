package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.CoordinateAccuracyService;
import com.example.localityconnector.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accuracy")
@RequiredArgsConstructor
@Tag(name = "Coordinate Accuracy", description = "Validate stored business coordinates")
public class CoordinateAccuracyController {

    private final BusinessService businessService;
    private final CoordinateAccuracyService coordinateAccuracyService;

    @Operation(summary = "Validate coordinates for all businesses")
    @GetMapping("/validate-all")
    public ResponseEntity<ApiResponse<Object>> validateAllBusinesses() {
        List<Business> businesses = businessService.getAllBusinesses();
        return ResponseEntity.ok(ApiResponse.ok(coordinateAccuracyService.validateAllBusinesses(businesses)));
    }

    @Operation(summary = "Validate coordinates for one business")
    @GetMapping("/validate/{businessId}")
    public ResponseEntity<ApiResponse<Object>> validateBusiness(@PathVariable String businessId) {
        Business business = businessService.findById(businessId).orElse(null);
        if (business == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(coordinateAccuracyService.validateBusinessCoordinates(business)));
    }

    @Operation(summary = "Validate a sample of businesses")
    @GetMapping("/sample-validation")
    public ResponseEntity<ApiResponse<Object>> sampleValidation(@RequestParam(defaultValue = "10") int limit) {
        List<Business> businesses = businessService.getAllBusinesses().stream().limit(limit).toList();
        return ResponseEntity.ok(ApiResponse.ok(coordinateAccuracyService.validateAllBusinesses(businesses)));
    }

    @Operation(summary = "Summary statistics of coordinate accuracy")
    @GetMapping("/accuracy-stats")
    public ResponseEntity<ApiResponse<Object>> getAccuracyStats() {
        List<Business> businesses = businessService.getAllBusinesses();
        Map<String, Object> results = coordinateAccuracyService.validateAllBusinesses(businesses);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total_businesses", results.get("total_businesses"));
        stats.put("valid_businesses", results.get("valid_businesses"));
        stats.put("validation_rate", results.get("validation_rate"));
        stats.put("high_accuracy_rate", results.get("high_accuracy_rate"));
        stats.put("high_accuracy_count", results.get("high_accuracy"));
        stats.put("medium_accuracy_count", results.get("medium_accuracy"));
        stats.put("low_accuracy_count", results.get("low_accuracy"));
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
