package com.example.localityconnector.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Validated request body for creating a catalog item/listing.
 * Replaces the previous untyped {@code Map<String, Object>} body so Bean Validation applies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddListingRequest {

    @NotBlank(message = "itemName is required")
    private String itemName;

    @NotNull(message = "itemPrice is required")
    @Min(value = 0, message = "itemPrice must not be negative")
    private Double itemPrice;

    private String itemDescription;

    private String category;
    private Integer stock;
    private Boolean available;
    private String imageUrl;
    private java.util.List<String> imageUrls;
}
