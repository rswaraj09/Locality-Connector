package com.example.localityconnector.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveLocationRequest {
    private String businessName;
    private Double businessLatitude;
    private Double businessLongitude;
}