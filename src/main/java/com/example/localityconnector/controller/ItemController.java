package com.example.localityconnector.controller;

import com.example.localityconnector.dto.AddListingRequest;
import com.example.localityconnector.dto.UpdateItemRequest;
import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.Item;
import com.example.localityconnector.service.BusinessService;
import com.example.localityconnector.service.ItemService;
import com.example.localityconnector.service.StorageService;
import com.example.localityconnector.util.ApiResponse;
import com.example.localityconnector.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catalog item management for the logged-in business. Ownership is enforced on every
 * mutating call using the JWT principal ({@link SecurityUtils#getLoggedInEntityId()}).
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Business catalog management")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final ItemService itemService;
    private final BusinessService businessService;
    private final StorageService storageService;

    @Operation(summary = "Create a catalog item for the logged-in business")
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@Valid @RequestBody AddListingRequest request) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        Optional<Business> businessOpt = businessService.findById(businessId);
        if (businessOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Business not found"));
        }
        Business business = businessOpt.get();

        Item item = new Item();
        item.setBusinessId(business.getId());
        item.setBusinessName(business.getBusinessName());
        item.setName(request.getItemName());
        item.setPrice(request.getItemPrice());
        item.setDescription(request.getItemDescription());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getStock() != null) item.setStock(request.getStock());
        if (request.getAvailable() != null) item.setAvailable(request.getAvailable());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getImageUrls() != null) item.setImageUrls(request.getImageUrls());
        item.prePersist();
        itemService.createItem(item);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "Item saved");
        data.put("itemId", item.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(data));
    }

    @Operation(summary = "List the logged-in business's items")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> list() {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        return ResponseEntity.ok(ApiResponse.ok(itemService.getItemsByBusinessId(businessId)));
    }

    @Operation(summary = "Update an owned item")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> update(@PathVariable String id,
                                                      @Valid @RequestBody UpdateItemRequest request) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        Optional<Item> existingOpt = itemService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Item not found"));
        }
        Item existing = existingOpt.get();
        if (!businessId.equals(existing.getBusinessId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("You do not have permission to modify this item"));
        }

        existing.setName(request.getItemName());
        existing.setPrice(request.getItemPrice());
        existing.setDescription(request.getItemDescription() == null ? "" : request.getItemDescription());
        if (request.getCategory() != null) existing.setCategory(request.getCategory());
        if (request.getStock() != null) existing.setStock(request.getStock());
        if (request.getAvailable() != null) existing.setAvailable(request.getAvailable());
        if (request.getImageUrl() != null) existing.setImageUrl(request.getImageUrl());
        if (request.getImageUrls() != null) existing.setImageUrls(request.getImageUrls());
        existing.prePersist();
        itemService.createItem(existing);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Item updated")));
    }

    @Operation(summary = "Delete an owned item")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable String id) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) {
            return unauthorized();
        }
        Optional<Item> existingOpt = itemService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Item not found"));
        }
        if (!businessId.equals(existingOpt.get().getBusinessId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("You do not have permission to delete this item"));
        }
        itemService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Item deleted")));
    }

    @Operation(summary = "Public: list items for a given business")
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<Object>> getItemsByBusinessId(@PathVariable String businessId) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getItemsByBusinessId(businessId)));
    }

    @Operation(summary = "Upload primary image for an item")
    @PostMapping("/{id}/image")
    public ResponseEntity<ApiResponse<Object>> uploadImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) return unauthorized();
        Optional<Item> existingOpt = itemService.findById(id);
        if (existingOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Item not found"));
        Item item = existingOpt.get();
        if (!businessId.equals(item.getBusinessId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("Permission denied"));

        try {
            if (item.getImageUrl() != null) storageService.deleteByUrl(item.getImageUrl());
            String url = storageService.uploadImage(file, "items");
            item.setImageUrl(url);
            item.prePersist();
            itemService.createItem(item);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Image uploaded", "imageUrl", url)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Failed to upload image: " + e.getMessage()));
        }
    }

    @Operation(summary = "Upload additional images for an item (max 5)")
    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<Object>> uploadImages(@PathVariable String id, @RequestParam("files") MultipartFile[] files) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) return unauthorized();
        Optional<Item> existingOpt = itemService.findById(id);
        if (existingOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Item not found"));
        Item item = existingOpt.get();
        if (!businessId.equals(item.getBusinessId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("Permission denied"));

        try {
            if (item.getImageUrls() == null) item.setImageUrls(new ArrayList<>());
            if (item.getImageUrls().size() + files.length > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("Maximum 5 images allowed per item"));
            }
            for (MultipartFile file : files) {
                String url = storageService.uploadImage(file, "item_photos");
                item.getImageUrls().add(url);
            }
            item.prePersist();
            itemService.createItem(item);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Images uploaded", "imageUrls", item.getImageUrls())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Failed to upload images: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete an additional image from an item")
    @DeleteMapping("/{id}/images")
    public ResponseEntity<ApiResponse<Object>> deleteImage(@PathVariable String id, @RequestParam("url") String url) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) return unauthorized();
        Optional<Item> existingOpt = itemService.findById(id);
        if (existingOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Item not found"));
        Item item = existingOpt.get();
        if (!businessId.equals(item.getBusinessId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("Permission denied"));

        if (item.getImageUrls() != null && item.getImageUrls().remove(url)) {
            storageService.deleteByUrl(url);
            item.prePersist();
            itemService.createItem(item);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Image removed", "imageUrls", item.getImageUrls())));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Image URL not found on item"));
    }

    @Operation(summary = "Update item stock")
    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Object>> updateStock(@PathVariable String id, @RequestParam Integer stock) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) return unauthorized();
        Optional<Item> existingOpt = itemService.findById(id);
        if (existingOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Item not found"));
        Item item = existingOpt.get();
        if (!businessId.equals(item.getBusinessId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("Permission denied"));

        item.setStock(Math.max(0, stock));
        item.prePersist();
        itemService.createItem(item);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Stock updated", "stock", item.getStock())));
    }

    @Operation(summary = "Update item availability")
    @PutMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Object>> updateAvailability(@PathVariable String id, @RequestParam boolean available) {
        String businessId = SecurityUtils.getLoggedInEntityId();
        if (businessId == null) return unauthorized();
        Optional<Item> existingOpt = itemService.findById(id);
        if (existingOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Item not found"));
        Item item = existingOpt.get();
        if (!businessId.equals(item.getBusinessId())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("Permission denied"));

        item.setAvailable(available);
        item.prePersist();
        itemService.createItem(item);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Availability updated", "available", item.isAvailable())));
    }

    private ResponseEntity<ApiResponse<Object>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Business not logged in"));
    }
}
