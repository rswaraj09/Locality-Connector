package com.example.localityconnector.controller;

import com.example.localityconnector.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostics")
@Tag(name = "Diagnostics", description = "Environment/configuration sanity checks")
public class DiagnosticsController {

    @Value("${mappls.client.id:}")
    private String clientId;

    @Value("${mappls.client.secret:}")
    private String clientSecret;

    @Value("${mappls.api.key:}")
    private String apiKey;

    @Operation(summary = "Report whether Mappls credentials are configured (presence only)")
    @GetMapping("/mappls-env")
    public ResponseEntity<ApiResponse<Object>> mapplsEnvStatus() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("MAPPLS_CLIENT_ID_present", clientId != null && !clientId.isBlank());
        data.put("MAPPLS_CLIENT_SECRET_present", clientSecret != null && !clientSecret.isBlank());
        data.put("MAPPLS_API_KEY_present", apiKey != null && !apiKey.isBlank());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
