package com.example.localityconnector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	private String id;

	@NotBlank(message = "Name is required")
	@Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
	private String name;

	@NotBlank(message = "Email is required")
	@Email(message = "Please provide a valid email address")
	private String email;

	// WRITE_ONLY: the password hash must never be serialized into API responses
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters long")
	private String password;

	@NotBlank(message = "Address is required")
	private String address;

	private String phoneNumber;

	private List<String> searchHistory = new ArrayList<>();

	private Map<String, Boolean> notificationPreferences = new HashMap<>();

	private Date createdAt;

	private Date updatedAt;

	private boolean isActive = true;

	private boolean emailVerified = false;

	// Pre-save method to set timestamps
	public void prePersist() {
		if (createdAt == null) {
			createdAt = new Date();
		}
		updatedAt = new Date();
	}
}
