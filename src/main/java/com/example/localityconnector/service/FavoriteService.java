package com.example.localityconnector.service;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.Favorite;
import com.example.localityconnector.repository.FavoriteFirestoreRepository;
import com.example.localityconnector.repository.BusinessFirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteFirestoreRepository favoriteRepository;
    private final BusinessFirestoreRepository businessRepository;

    public Favorite addFavorite(String userId, String businessId) {
        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndBusinessId(userId, businessId)) {
            throw new IllegalArgumentException("Business is already in your favorites");
        }

        Optional<Business> businessOpt = businessRepository.findById(businessId);
        if (businessOpt.isEmpty()) {
            throw new com.example.localityconnector.exception.ResourceNotFoundException(
                    "Business", "id", businessId);
        }

        Business business = businessOpt.get();
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setBusinessId(businessId);
        favorite.setBusinessName(business.getBusinessName());
        favorite.setBusinessCategory(business.getCategory());
        favorite.prePersist();
        return favoriteRepository.save(favorite);
    }

    public void removeFavorite(String userId, String businessId) {
        favoriteRepository.deleteByUserIdAndBusinessId(userId, businessId);
    }

    public List<Favorite> getUserFavorites(String userId) {
        return favoriteRepository.findByUserId(userId);
    }

    public List<Favorite> getBusinessFavorites(String businessId) {
        return favoriteRepository.findByBusinessId(businessId);
    }

    public boolean isFavorited(String userId, String businessId) {
        return favoriteRepository.existsByUserIdAndBusinessId(userId, businessId);
    }

    public boolean toggleFavorite(String userId, String businessId) {
        if (isFavorited(userId, businessId)) {
            removeFavorite(userId, businessId);
            return false;
        } else {
            addFavorite(userId, businessId);
            return true;
        }
    }

    public java.util.Map<String, Object> getUserFavoritesPaginated(String userId, int page, int size) {
        List<Favorite> all = getUserFavorites(userId);
        int total = all.size();
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 0);
        int start = Math.min(safePage * safeSize, total);
        int end = Math.min(start + safeSize, total);
        List<Favorite> content = all.subList(start, end);

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("favorites", content);
        response.put("currentPage", safePage);
        response.put("totalPages", (int) Math.ceil((double) total / safeSize));
        response.put("totalElements", total);
        response.put("size", safeSize);
        return response;
    }
}
