package com.example.localityconnector.controller;

import com.example.localityconnector.model.Favorite;
import com.example.localityconnector.service.FavoriteService;
import com.example.localityconnector.util.ApiResponse;
import com.example.localityconnector.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "User bookmark / favorites management")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "Add a business to favorites")
    @PostMapping("/{businessId}")
    public ResponseEntity<ApiResponse<Object>> addFavorite(@PathVariable String businessId) {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        Favorite favorite = favoriteService.addFavorite(userId, businessId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of("message", "Added to favorites", "favoriteId", favorite.getId())));
    }

    @Operation(summary = "Remove a business from favorites")
    @DeleteMapping("/{businessId}")
    public ResponseEntity<ApiResponse<Object>> removeFavorite(@PathVariable String businessId) {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        favoriteService.removeFavorite(userId, businessId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "Removed from favorites")));
    }

    @Operation(summary = "List all favorites for the logged-in user (optional pagination)")
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> listFavorites(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        if (page != null && size != null) {
            return ResponseEntity.ok(ApiResponse.ok(favoriteService.getUserFavoritesPaginated(userId, page, size)));
        }
        List<Favorite> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(ApiResponse.ok(favorites));
    }

    @Operation(summary = "Toggle favorite status for a business")
    @PostMapping("/{businessId}/toggle")
    public ResponseEntity<ApiResponse<Object>> toggleFavorite(@PathVariable String businessId) {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        boolean nowFavorited = favoriteService.toggleFavorite(userId, businessId);
        String msg = nowFavorited ? "Added to favorites" : "Removed from favorites";
        return ResponseEntity.ok(ApiResponse.ok(Map.of("favorited", nowFavorited, "message", msg)));
    }

    @Operation(summary = "Check if a business is in the user's favorites")
    @GetMapping("/{businessId}/status")
    public ResponseEntity<ApiResponse<Object>> checkStatus(@PathVariable String businessId) {
        String userId = SecurityUtils.getLoggedInEntityId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Not logged in"));
        }
        boolean favorited = favoriteService.isFavorited(userId, businessId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("favorited", favorited)));
    }
}
