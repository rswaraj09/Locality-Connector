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

    /**
     * Prefix search on businessName (case-insensitive). Uses Spring Data's derived
     * query method rather than a hand-written {@code $regex} string: Spring Data
     * escapes regex metacharacters in the parameter automatically, whereas splicing
     * the raw prefix into a {@code @Query} regex string let a caller inject regex
     * syntax (e.g. catastrophic-backtracking patterns) through the public
     * {@code /api/business-data/search} endpoint.
     */
    List<Business> findByBusinessNameStartingWithIgnoreCase(String prefix);

    long countByBusinessNameStartingWithIgnoreCase(String prefix);

    /** Paginated query ordered by a field. */
    Page<Business> findAll(Pageable pageable);
}
