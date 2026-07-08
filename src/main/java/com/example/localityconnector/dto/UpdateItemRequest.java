package com.example.localityconnector.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Validated request body for updating a catalog item. Mirrors {@link AddListingRequest}
 * and replaces the previous untyped {@code Map<String, Object>} body + hand-rolled type
 * coercion in {@code ItemController.update} so malformed input gets a clean 400.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    @NotBlank(message = "itemName is required")
    @Size(max = 200, message = "itemName must be at most 200 characters")
    private String itemName;

    @NotNull(message = "itemPrice is required")
    @Min(value = 0, message = "itemPrice must not be negative")
    private Double itemPrice;

    @Size(max = 2000, message = "itemDescription must be at most 2000 characters")
    private String itemDescription;

    private String category;
    private Integer stock;
    private Boolean available;
    private String imageUrl;
    private java.util.List<String> imageUrls;
}
