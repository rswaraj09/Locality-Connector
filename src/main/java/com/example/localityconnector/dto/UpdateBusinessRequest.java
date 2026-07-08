package com.example.localityconnector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Validated request body for updating a business profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBusinessRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Business type is required")
    private String businessType;

    private String businessDescription;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;
}
