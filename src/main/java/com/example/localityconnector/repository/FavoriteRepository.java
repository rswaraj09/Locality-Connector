package com.example.localityconnector.repository;

import com.example.localityconnector.model.Favorite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends MongoRepository<Favorite, String> {

    List<Favorite> findByUserId(String userId);

    List<Favorite> findByBusinessId(String businessId);

    Optional<Favorite> findByUserIdAndBusinessId(String userId, String businessId);

    boolean existsByUserIdAndBusinessId(String userId, String businessId);

    void deleteByUserIdAndBusinessId(String userId, String businessId);
}
