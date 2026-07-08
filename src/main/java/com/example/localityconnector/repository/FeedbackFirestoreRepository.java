package com.example.localityconnector.repository;

import com.example.localityconnector.model.Feedback;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class FeedbackFirestoreRepository extends BaseFirestoreRepository<Feedback> {

    private static final String COLLECTION_NAME = "feedback";

    /** Firestore caps a single WriteBatch at 500 operations; stay safely below that. */
    private static final int BATCH_LIMIT = 450;

    public FeedbackFirestoreRepository(Firestore firestore) {
        super(firestore, COLLECTION_NAME, Feedback.class);
    }

    public Feedback save(Feedback feedback) {
        if (feedback.getId() == null) {
            feedback.setId(newId());
        }
        await(collection().document(feedback.getId()).set(feedback), "Failed to save feedback");
        return feedback;
    }

    public List<Feedback> findByBusinessId(String businessId) {
        return queryList(collection().whereEqualTo("businessId", businessId),
                "Failed to fetch feedback by businessId");
    }

    public List<Feedback> findByBusinessIdOrderByCreatedAtDesc(String businessId) {
        return queryList(collection()
                        .whereEqualTo("businessId", businessId)
                        .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING),
                "Failed to fetch feedback by businessId ordered by createdAt. "
                        + "Ensure the composite index (businessId ASC, createdAt DESC) is deployed.");
    }

    public List<Feedback> findByUserId(String userId) {
        return queryList(collection().whereEqualTo("userId", userId),
                "Failed to fetch feedback by userId");
    }

    /**
     * Compute the average star rating for a business.
     *
     * @return the average (0.0 when there is no feedback)
     */
    public double averageRatingByBusinessId(String businessId) {
        List<Feedback> feedbackList = findByBusinessId(businessId);
        int count = 0;
        long sum = 0;
        for (Feedback feedback : feedbackList) {
            if (feedback.getRating() != null) {
                sum += feedback.getRating();
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        return Math.round(((double) sum / count) * 10.0) / 10.0;
    }

    /**
     * Keep the denormalised {@code businessName} on every feedback entry in sync when a
     * business renames itself, committing the updates in batched single round-trips.
     *
     * @return the number of feedback records updated
     */
    public int updateBusinessNameOnFeedback(String businessId, String newBusinessName) {
        List<QueryDocumentSnapshot> documents = await(
                collection().whereEqualTo("businessId", businessId).get(),
                "Failed to fetch feedback for rename").getDocuments();
        Date now = new Date();
        int updated = 0;
        int index = 0;
        while (index < documents.size()) {
            WriteBatch batch = firestore.batch();
            int end = Math.min(index + BATCH_LIMIT, documents.size());
            for (; index < end; index++) {
                batch.update(documents.get(index).getReference(),
                        "businessName", newBusinessName, "updatedAt", now);
                updated++;
            }
            await(batch.commit(), "Failed to commit feedback businessName update batch");
        }
        return updated;
    }

    /**
     * Cascade-delete every feedback entry for a business using batched commits.
     *
     * @return the number of feedback records removed
     */
    public int deleteByBusinessId(String businessId) {
        List<QueryDocumentSnapshot> documents = await(
                collection().whereEqualTo("businessId", businessId).get(),
                "Failed to fetch feedback for delete").getDocuments();
        int removed = 0;
        int index = 0;
        while (index < documents.size()) {
            WriteBatch batch = firestore.batch();
            int end = Math.min(index + BATCH_LIMIT, documents.size());
            for (; index < end; index++) {
                batch.delete(documents.get(index).getReference());
                removed++;
            }
            await(batch.commit(), "Failed to commit feedback delete batch");
        }
        return removed;
    }
}
