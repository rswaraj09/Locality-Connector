package com.example.localityconnector.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Login credentials. Bean Validation rejects malformed/empty bodies with a clean 400
 * (via GlobalExceptionHandler) before the authentication flow runs.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 1, message = "Password is required")
    private String password;
}
