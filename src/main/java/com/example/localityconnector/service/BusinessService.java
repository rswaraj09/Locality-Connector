package com.example.localityconnector.service;

import com.example.localityconnector.dto.BusinessSignupRequest;
import com.example.localityconnector.dto.LocationBasedBusinessRequest;
import com.example.localityconnector.dto.PaginatedResult;
import com.example.localityconnector.exception.DuplicateResourceException;
import com.example.localityconnector.exception.ResourceNotFoundException;
import com.example.localityconnector.model.Business;
import com.example.localityconnector.repository.BusinessFirestoreRepository;
import com.example.localityconnector.util.GeolocationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessFirestoreRepository businessRepository;
    private final GooglePlacesService googlePlacesService;
    private final ItemService itemService;
    private final FeedbackService feedbackService;
    private final PasswordEncoder passwordEncoder;

    public Business signup(BusinessSignupRequest request) {
        if (businessRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Business", "email", request.getEmail());
        }
        if (businessRepository.existsByBusinessName(request.getBusinessName())) {
            throw new DuplicateResourceException("Business", "businessName", request.getBusinessName());
        }

        Business business = new Business();
        business.setBusinessName(request.getBusinessName());
        business.setOwnerName(request.getOwnerName());
        business.setEmail(request.getEmail());
        business.setPassword(passwordEncoder.encode(request.getPassword()));
        business.setAddress(request.getAddress());
        business.setPhoneNumber(request.getPhoneNumber());
        business.setCategory(request.getCategory());
        business.setDescription(request.getDescription());
        business.setBusinessLicense(request.getBusinessLicense());
        business.prePersist();

        Business saved = businessRepository.save(business);

        // Best-effort geocoding: resolve coordinates + geohash from the address.
        // Failures here must never block signup.
        applyGeocoding(saved);
        return saved;
    }

    /** Geocode the business address and persist coordinates + geohash if resolved. */
    private void applyGeocoding(Business business) {
        try {
            Map<String, Double> coordinates = googlePlacesService.geocodeAddress(business.getAddress());
            if (coordinates != null && coordinates.get("lat") != null && coordinates.get("lng") != null) {
                double lat = coordinates.get("lat");
                double lng = coordinates.get("lng");
                business.setLatitude(lat);
                business.setLongitude(lng);
                business.setGeohash(GeolocationUtils.computeGeohash(lat, lng));
                businessRepository.save(business);
            }
        } catch (Exception e) {
            log.warn("Geocoding failed for business '{}': {}", business.getBusinessName(), e.getMessage());
        }
    }

    public Optional<Business> login(String email, String password) {
        Optional<Business> businessOpt = businessRepository.findByEmail(email);
        if (businessOpt.isPresent() && passwordEncoder.matches(password, businessOpt.get().getPassword())) {
            return businessOpt;
        }
        return Optional.empty();
    }

    public Optional<Business> findByEmail(String email) {
        return businessRepository.findByEmail(email);
    }

    public Optional<Business> findByBusinessName(String businessName) {
        return businessRepository.findByBusinessName(businessName);
    }

    @Cacheable(value = "businesses", key = "'all'")
    public List<Business> getAllBusinesses() {
        return businessRepository.findAll();
    }

    public PaginatedResult<Business> getBusinessesPaginated(int limit, String startAfterId) {
        return businessRepository.findAllPaginated(limit, startAfterId);
    }

    public List<Business> getBusinessesByCategory(String category) {
        return businessRepository.findByCategoryAndIsActiveTrue(category);
    }

    @Cacheable(value = "businesses", key = "#id")
    public Optional<Business> findById(String id) {
        return businessRepository.findById(id);
    }

    @CacheEvict(value = "businesses", allEntries = true)
    public Business updateBusiness(String id, Business businessDetails) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", id));

        String previousName = business.getBusinessName();
        boolean nameChanged = businessDetails.getBusinessName() != null
                && !businessDetails.getBusinessName().equals(previousName);

        business.setBusinessName(businessDetails.getBusinessName());
        business.setOwnerName(businessDetails.getOwnerName());
        business.setAddress(businessDetails.getAddress());
        business.setPhoneNumber(businessDetails.getPhoneNumber());
        business.setCategory(businessDetails.getCategory());
        business.setDescription(businessDetails.getDescription());
        business.setBusinessLicense(businessDetails.getBusinessLicense());
        business.prePersist();

        Business saved = businessRepository.save(business);

        // Keep denormalised business name on catalog items AND feedback in sync.
        if (nameChanged) {
            int items = itemService.updateBusinessNameOnItems(saved.getId(), saved.getBusinessName());
            int feedback = feedbackService.updateBusinessNameOnFeedback(saved.getId(), saved.getBusinessName());
            log.info("Propagated business rename to {} item(s) and {} feedback record(s) for business {}",
                    items, feedback, saved.getId());
        }
        return saved;
    }

    /**
     * Update the logged-in business's geolocation (latitude/longitude) and recompute its
     * geohash. Replaces the deleted HomeController {@code /saveLocation} endpoint.
     */
    public Business updateLocation(String id, double latitude, double longitude) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", id));
        business.setLatitude(latitude);
        business.setLongitude(longitude);
        business.setGeohash(GeolocationUtils.computeGeohash(latitude, longitude));
        business.prePersist();
        return businessRepository.save(business);
    }

    public Business verifyBusiness(String id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", id));
        business.setVerified(true);
        business.prePersist();
        return businessRepository.save(business);
    }

    public Business setActive(String id, boolean active) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "id", id));
        business.setActive(active);
        business.prePersist();
        return businessRepository.save(business);
    }

    @CacheEvict(value = "businesses", allEntries = true)
    public void deleteBusiness(String id) {
        businessRepository.deleteById(id);
    }

    public List<Business> getBusinessesWithinRadius(LocationBasedBusinessRequest request) {
        double lat = request.getLatitude();
        double lng = request.getLongitude();
        double radiusKm = request.getRadiusKm();

        List<Business> candidates = queryGeohashCandidates(lat, lng, radiusKm);
        if (candidates.isEmpty()) {
            // No geohash-indexed data (e.g. legacy rows): fall back to a full scan.
            candidates = businessRepository.findAll();
        }

        boolean hasCoords = candidates.stream()
                .anyMatch(b -> b.getLatitude() != null && b.getLongitude() != null);

        if (!hasCoords) {
            return candidates.stream()
                    .filter(b -> matchesCategory(b, request.getCategory()))
                    .collect(Collectors.toList());
        }

        return candidates.stream()
                .filter(b -> b.getLatitude() != null && b.getLongitude() != null)
                .filter(b -> GeolocationUtils.isWithinRadius(lat, lng, b.getLatitude(), b.getLongitude(), radiusKm))
                .filter(b -> matchesCategory(b, request.getCategory()))
                .collect(Collectors.toList());
    }

    private List<Business> queryGeohashCandidates(double lat, double lng, double radiusKm) {
        try {
            int precision = GeolocationUtils.precisionForRadiusKm(radiusKm);
            List<String> prefixes = GeolocationUtils.getNeighborGeohashes(lat, lng, precision);
            Map<String, Business> byId = new LinkedHashMap<>();
            for (String prefix : prefixes) {
                for (Business business : businessRepository.findByGeohashPrefix(prefix)) {
                    if (business.getId() != null) {
                        byId.put(business.getId(), business);
                    }
                }
            }
            return new ArrayList<>(byId.values());
        } catch (Exception e) {
            log.warn("Geohash proximity query failed, falling back to full scan: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean matchesCategory(Business business, String category) {
        if (category == null || category.isEmpty()) {
            return true;
        }
        return category.equalsIgnoreCase(business.getCategory());
    }

    public List<Business> getBusinessesWithinRadius(double latitude, double longitude, double radiusKm) {
        return getBusinessesWithinRadius(new LocationBasedBusinessRequest(latitude, longitude, radiusKm));
    }

    public List<Business> getBusinessesWithinRadius(double latitude, double longitude) {
        return getBusinessesWithinRadius(latitude, longitude, 5.0);
    }

    // ------------------------------------------------------------------
    // Delegated query methods (used by controllers that previously
    // accessed the repository directly — maintaining clean layering).
    // ------------------------------------------------------------------

    public List<Business> findAllSorted(String sortField, boolean descending, int offset, int limit) {
        return businessRepository.findAllSorted(sortField, descending, offset, limit);
    }

    public long countAll() {
        return businessRepository.countAll();
    }

    public long countByCategory(String category) {
        return businessRepository.countByCategory(category);
    }

    public long countWithCoordinates() {
        return businessRepository.countWithCoordinates();
    }

    public List<Business> findByLatitudeRange(double minLat, double maxLat) {
        return businessRepository.findByLatitudeRange(minLat, maxLat);
    }

    public List<Business> searchByNamePrefix(String prefix, int offset, int limit) {
        return businessRepository.searchByNamePrefix(prefix, offset, limit);
    }

    public long countByNamePrefix(String prefix) {
        return businessRepository.countByNamePrefix(prefix);
    }

    public List<Business> findAllExport() {
        return businessRepository.findAll();
    }

    public boolean existsByEmail(String email) {
        return businessRepository.existsByEmail(email);
    }

    public List<Business> getUnverifiedBusinesses() {
        return businessRepository.findByVerifiedFalse();
    }

    @CacheEvict(value = "businesses", allEntries = true)
    public Business saveBusiness(Business business) {
        business.prePersist();
        return businessRepository.save(business);
    }
}

