package com.example.localityconnector.repository;

import com.example.localityconnector.model.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends MongoRepository<Business, String> {

    Optional<Business> findByEmail(String email);

    Optional<Business> findByBusinessName(String businessName);

    boolean existsByEmail(String email);

    boolean existsByBusinessName(String businessName);

    List<Business> findByCategoryAndIsActiveTrue(String category);

    List<Business> findByGeohashStartingWith(String prefix);

    List<Business> findByIsVerifiedFalse();

    long countByCategory(String category);

    @Query(value = "{'latitude': {$gte: ?0, $lte: ?1}}")
    List<Business> findByLatitudeRange(double minLat, double maxLat);

    /** Count businesses that have a stored latitude (i.e. geocoded). */
    long countByLatitudeGreaterThanEqual(double minLat);

    /** Prefix search on businessName (case-insensitive regex). */
    @Query(value = "{'businessName': {$regex: '^?0', $options: 'i'}}")
    List<Business> findByBusinessNameStartingWithIgnoreCase(String prefix);

    @Query(value = "{'businessName': {$regex: '^?0', $options: 'i'}}", count = true)
    long countByBusinessNameStartingWithIgnoreCase(String prefix);

    /** Paginated query ordered by a field. */
    Page<Business> findAll(Pageable pageable);
}
