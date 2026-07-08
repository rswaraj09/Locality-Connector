package com.example.localityconnector.repository;

import com.example.localityconnector.dto.PaginatedResult;
import com.example.localityconnector.model.Business;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BusinessFirestoreRepository extends BaseFirestoreRepository<Business> {

    private static final String COLLECTION_NAME = "businesses";

    public BusinessFirestoreRepository(Firestore firestore) {
        super(firestore, COLLECTION_NAME, Business.class);
    }

    public Business save(Business business) {
        if (business.getId() == null) {
            business.setId(newId());
        }
        await(collection().document(business.getId()).set(business), "Failed to save business");
        return business;
    }

    public java.util.Optional<Business> findByEmail(String email) {
        return queryOne(collection().whereEqualTo("email", email), "Failed to fetch business by email");
    }

    public java.util.Optional<Business> findByBusinessName(String businessName) {
        return queryOne(collection().whereEqualTo("businessName", businessName), "Failed to fetch business by name");
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public boolean existsByBusinessName(String businessName) {
        return findByBusinessName(businessName).isPresent();
    }

    /**
     * Push the category + active filter into Firestore. Lombok generates {@code isActive()}
     * for the {@code boolean isActive} field, which Firestore serialises as {@code "active"}.
     * Requires the composite index (category ASC, active ASC) declared in firestore.indexes.json.
     */
    public List<Business> findByCategoryAndIsActiveTrue(String category) {
        return queryList(collection().whereEqualTo("category", category).whereEqualTo("active", true),
                "Failed to query businesses by category. Ensure the composite index "
                        + "(category ASC, active ASC) from firestore.indexes.json is deployed.");
    }

    /**
     * Range query over the {@code geohash} prefix using the Firestore string-range trick
     * ({@code prefix} .. {@code prefix + \uf8ff}).
     */
    public List<Business> findByGeohashPrefix(String prefix) {
        return queryList(collection()
                        .whereGreaterThanOrEqualTo("geohash", prefix)
                        .whereLessThanOrEqualTo("geohash", prefix + "\uf8ff"),
                "Failed to query businesses by geohash prefix");
    }

    /** Query unverified businesses directly from Firestore (replaces in-memory filtering). */
    public List<Business> findByVerifiedFalse() {
        return queryList(collection().whereEqualTo("verified", false),
                "Failed to query unverified businesses");
    }

    // -----------------------------------------------------------------
    // Server-side pushdown queries backing BusinessDataController
    // -----------------------------------------------------------------

    /** Ordered + offset/limited page, sorted server-side on a single field. */
    public List<Business> findAllSorted(String sortField, boolean descending, int offset, int limit) {
        Query.Direction direction = descending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING;
        Query query = collection()
                .orderBy(sortField, direction)
                .offset(Math.max(offset, 0))
                .limit(Math.max(limit, 1));
        return queryList(query, "Failed to query businesses sorted by " + sortField);
    }

    /** Total document count via Firestore aggregation (no document reads). */
    public long countAll() {
        return countQuery(collection(), "Failed to count businesses");
    }

    public long countByCategory(String category) {
        return countQuery(collection().whereEqualTo("category", category),
                "Failed to count businesses for category " + category);
    }

    /** Count of businesses that have a stored latitude (i.e. geocoded). */
    public long countWithCoordinates() {
        return countQuery(collection().whereGreaterThanOrEqualTo("latitude", -90.0),
                "Failed to count geo-tagged businesses");
    }

    /** Single-field range query on latitude; used to narrow the by-state scan server-side. */
    public List<Business> findByLatitudeRange(double minLat, double maxLat) {
        return queryList(collection()
                        .whereGreaterThanOrEqualTo("latitude", minLat)
                        .whereLessThanOrEqualTo("latitude", maxLat),
                "Failed to query businesses by latitude range");
    }

    /** Prefix search on businessName using orderBy + startAt/endAt (Firestore has no substring search). */
    public List<Business> searchByNamePrefix(String prefix, int offset, int limit) {
        Query query = collection()
                .orderBy("businessName")
                .startAt(prefix)
                .endAt(prefix + "\uf8ff")
                .offset(Math.max(offset, 0))
                .limit(Math.max(limit, 1));
        return queryList(query, "Failed to search businesses by name prefix");
    }

    public long countByNamePrefix(String prefix) {
        return countQuery(collection()
                        .orderBy("businessName")
                        .startAt(prefix)
                        .endAt(prefix + "\uf8ff"),
                "Failed to count businesses by name prefix");
    }

    /**
     * Native cursor pagination ordered by document id.
     *
     * @param limit        page size
     * @param startAfterId document id of the last item from the previous page (nullable)
     */
    public PaginatedResult<Business> findAllPaginated(int limit, String startAfterId) {
        Query query = collection().orderBy(FieldPath.documentId()).limit(limit);
        if (startAfterId != null && !startAfterId.isBlank()) {
            DocumentSnapshot startDoc = await(collection().document(startAfterId).get(),
                    "Failed to load pagination cursor");
            if (startDoc.exists()) {
                query = collection().orderBy(FieldPath.documentId()).startAfter(startDoc).limit(limit);
            }
        }
        List<QueryDocumentSnapshot> documents = await(query.get(), "Failed to paginate businesses").getDocuments();
        List<Business> businesses = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            businesses.add(document.toObject(Business.class));
        }
        String nextCursor = documents.isEmpty() ? null : documents.get(documents.size() - 1).getId();
        boolean hasMore = documents.size() == limit;
        return new PaginatedResult<>(businesses, hasMore ? nextCursor : null, hasMore);
    }

    public long count() {
        return countAll();
    }

    public void saveAll(List<Business> businesses) {
        for (Business business : businesses) {
            save(business);
        }
    }

    public void deleteAll() {
        List<QueryDocumentSnapshot> documents = await(collection().get(),
                "Failed to delete all businesses").getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            document.getReference().delete();
        }
    }

    /**
     * Atomically update ratingSum, ratingCount, and averageRating inside a Firestore transaction.
     */
    public void updateRatingAtomically(String businessId, int ratingDelta, int countDelta) {
        await(firestore.runTransaction(transaction -> {
            DocumentReference docRef = collection().document(businessId);
            DocumentSnapshot snapshot = transaction.get(docRef).get();
            if (snapshot.exists()) {
                long currentSum = snapshot.contains("ratingSum") && snapshot.get("ratingSum") != null ? snapshot.getLong("ratingSum") : 0L;
                long currentCount = snapshot.contains("ratingCount") && snapshot.get("ratingCount") != null ? snapshot.getLong("ratingCount") : 0L;
                
                long newSum = Math.max(0, currentSum + ratingDelta);
                long newCount = Math.max(0, currentCount + countDelta);
                double newAvg = newCount > 0 ? Math.round(((double) newSum / newCount) * 10.0) / 10.0 : 0.0;
                
                transaction.update(docRef, "ratingSum", newSum, "ratingCount", (int) newCount, "averageRating", newAvg);
            }
            return null;
        }), "Failed to atomically update rating");
    }
}
