package com.example.localityconnector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendOtpRequest {

    @NotBlank(message = "Target email or phone number is required")
    private String target;

    @NotBlank(message = "Target type is required (EMAIL or PHONE)")
    private String targetType;
}
