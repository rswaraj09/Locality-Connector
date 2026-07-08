package com.example.localityconnector.repository;

import com.example.localityconnector.model.Favorite;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FavoriteFirestoreRepository extends BaseFirestoreRepository<Favorite> {

    public FavoriteFirestoreRepository(Firestore firestore) {
        super(firestore, "favorites", Favorite.class);
    }

    public Favorite save(Favorite favorite) {
        if (favorite.getId() == null) {
            favorite.setId(newId());
        }
        await(collection().document(favorite.getId()).set(favorite), "Failed to save favorite");
        return favorite;
    }

    public List<Favorite> findByUserId(String userId) {
        return queryList(collection().whereEqualTo("userId", userId),
                "Failed to fetch favorites for user");
    }

    public List<Favorite> findByBusinessId(String businessId) {
        return queryList(collection().whereEqualTo("businessId", businessId),
                "Failed to fetch favorites for business");
    }

    public Optional<Favorite> findByUserIdAndBusinessId(String userId, String businessId) {
        return queryOne(collection()
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("businessId", businessId),
                "Failed to check favorite status");
    }

    public boolean existsByUserIdAndBusinessId(String userId, String businessId) {
        return findByUserIdAndBusinessId(userId, businessId).isPresent();
    }

    public void deleteByUserIdAndBusinessId(String userId, String businessId) {
        findByUserIdAndBusinessId(userId, businessId)
                .ifPresent(fav -> deleteById(fav.getId()));
    }
}
