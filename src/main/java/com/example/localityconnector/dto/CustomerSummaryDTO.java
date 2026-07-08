package com.example.localityconnector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal projection of a customer exposed to a business dashboard.
 * Intentionally contains no password hash, address or other sensitive fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDTO {
    private String name;
    private String email;
}
