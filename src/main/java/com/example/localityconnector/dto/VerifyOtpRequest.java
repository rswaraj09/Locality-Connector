package com.example.localityconnector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Target email or phone number is required")
    private String target;

    @NotBlank(message = "4-digit OTP code is required")
    @Pattern(regexp = "^\\d{4}$", message = "OTP code must be exactly 4 digits")
    private String otpCode;
}
