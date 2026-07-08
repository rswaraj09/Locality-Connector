package com.example.localityconnector.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveLocationRequest {
    private String businessName;

    @NotNull(message = "Latitude is required")
    private Double businessLatitude;

    @NotNull(message = "Longitude is required")
    private Double businessLongitude;
}
