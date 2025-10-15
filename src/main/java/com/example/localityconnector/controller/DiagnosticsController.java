package com.example.localityconnector.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticsController {

	@Value("${mappls.client.id:}")
	private String clientId;

	@Value("${mappls.client.secret:}")
	private String clientSecret;

	@Value("${mappls.api.key:}")
	private String apiKey;

	@GetMapping("/mappls-env")
	public ResponseEntity<?> mapplsEnvStatus() {
		boolean hasClientId = clientId != null && !clientId.isBlank();
		boolean hasClientSecret = clientSecret != null && !clientSecret.isBlank();
		boolean hasApiKey = apiKey != null && !apiKey.isBlank();
		return ResponseEntity.ok(
			Map.of(
				"MAPPLS_CLIENT_ID_present", hasClientId,
				"MAPPLS_CLIENT_SECRET_present", hasClientSecret,
				"MAPPLS_API_KEY_present", hasApiKey
			)
		);
	}
}

