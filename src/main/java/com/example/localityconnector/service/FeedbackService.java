package com.example.localityconnector.service;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.Feedback;
import com.example.localityconnector.repository.BusinessFirestoreRepository;
import com.example.localityconnector.repository.FeedbackFirestoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackFirestoreRepository feedbackRepository;
    private final BusinessFirestoreRepository businessRepository;

    public Feedback createFeedback(Feedback feedback) {
        feedback.prePersist();
        return feedbackRepository.save(feedback);
    }

    /**
     * Submit a star-rated review. Updates the denormalized rating counters on the
     * business document so average rating never requires scanning all feedback.
     */
    public Feedback submitFeedback(String businessId, String businessName, String userId,
                                   String userName, String userEmail, int rating, String comment) {
        Feedback feedback = new Feedback();
        feedback.setBusinessId(businessId);
        feedback.setBusinessName(businessName);
        feedback.setUserId(userId);
        feedback.setUserName(userName);
        feedback.setUserEmail(userEmail);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.prePersist();
        Feedback saved = feedbackRepository.save(feedback);

        // Update denormalized rating on the business
        updateBusinessRating(businessId, rating, true);

        return saved;
    }

    /**
     * Get average rating from the denormalized business fields (O(1) read).
     * Falls back to computing from feedback if the business has no counters yet.
     */
    public double getAverageRating(String businessId) {
        Optional<Business> businessOpt = businessRepository.findById(businessId);
        if (businessOpt.isPresent()) {
            Business business = businessOpt.get();
            if (business.getRatingCount() > 0) {
                return business.getAverageRating();
            }
        }
        // Fallback for legacy data without denormalized counters
        return feedbackRepository.averageRatingByBusinessId(businessId);
    }

    public List<Feedback> getFeedbackByBusinessId(String businessId) {
        return feedbackRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    public List<Feedback> getFeedbackByUserId(String userId) {
        return feedbackRepository.findByUserId(userId);
    }

    public Optional<Feedback> findById(String id) {
        return feedbackRepository.findById(id);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    /**
     * Update an existing feedback entry (by the original author).
     * Adjusts the denormalized rating counters on the business.
     */
    public Feedback updateFeedback(String feedbackId, int newRating, String newComment) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new com.example.localityconnector.exception.ResourceNotFoundException(
                        "Feedback", "id", feedbackId));
        int oldRating = feedback.getRating() != null ? feedback.getRating() : 0;

        feedback.setRating(newRating);
        feedback.setComment(newComment);
        feedback.prePersist();
        Feedback saved = feedbackRepository.save(feedback);

        // Adjust denormalized counters: subtract old, add new
        adjustBusinessRating(feedback.getBusinessId(), oldRating, newRating);

        return saved;
    }

    /**
     * Delete a feedback entry. Adjusts the denormalized rating counters.
     */
    public void deleteFeedback(String feedbackId) {
        Optional<Feedback> feedbackOpt = feedbackRepository.findById(feedbackId);
        if (feedbackOpt.isPresent()) {
            Feedback feedback = feedbackOpt.get();
            int rating = feedback.getRating() != null ? feedback.getRating() : 0;
            feedbackRepository.deleteById(feedbackId);
            updateBusinessRating(feedback.getBusinessId(), rating, false);
        } else {
            feedbackRepository.deleteById(feedbackId);
        }
    }

    /**
     * Propagate a business rename to all of its feedback entries (denormalised
     * {@code businessName}), batched into single round-trips.
     *
     * @return the number of feedback records updated
     */
    public int updateBusinessNameOnFeedback(String businessId, String newBusinessName) {
        return feedbackRepository.updateBusinessNameOnFeedback(businessId, newBusinessName);
    }

    /**
     * Cascade-delete every feedback entry for a business in a single batched round-trip.
     *
     * @return the number removed.
     */
    public int deleteFeedbackByBusinessId(String businessId) {
        return feedbackRepository.deleteByBusinessId(businessId);
    }

    public Feedback reportFeedback(String feedbackId, String reason) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new com.example.localityconnector.exception.ResourceNotFoundException(
                        "Feedback", "id", feedbackId));
        feedback.setFlagged(true);
        String currentNote = feedback.getModerationNote() != null ? feedback.getModerationNote() + "; " : "";
        feedback.setModerationNote(currentNote + "Reported: " + (reason != null ? reason : "No reason provided"));
        feedback.prePersist();
        return feedbackRepository.save(feedback);
    }

    public java.util.Map<String, Object> getRatingHistogram(String businessId) {
        List<Feedback> feedbackList = getFeedbackByBusinessId(businessId);
        java.util.Map<Integer, Long> distribution = new java.util.LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            distribution.put(i, 0L);
        }
        long sum = 0;
        for (Feedback f : feedbackList) {
            if (f.getRating() != null && f.getRating() >= 1 && f.getRating() <= 5) {
                distribution.put(f.getRating(), distribution.get(f.getRating()) + 1);
                sum += f.getRating();
            }
        }
        long total = feedbackList.size();
        double avg = total > 0 ? Math.round(((double) sum / total) * 100.0) / 100.0 : 0.0;

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("businessId", businessId);
        result.put("histogram", distribution);
        result.put("totalReviews", total);
        result.put("averageRating", avg);
        return result;
    }

    // -------------------------------------------------------------------
    // Denormalized rating helpers
    // -------------------------------------------------------------------

    /** Add or remove a rating from the business counters. */
    private void updateBusinessRating(String businessId, int rating, boolean add) {
        try {
            if (add) {
                businessRepository.updateRatingAtomically(businessId, rating, 1);
            } else {
                businessRepository.updateRatingAtomically(businessId, -rating, -1);
            }
        } catch (Exception e) {
            log.warn("Failed to update denormalized rating for business {}: {}", businessId, e.getMessage());
        }
    }

    /** Adjust counters when a rating changes (edit). */
    private void adjustBusinessRating(String businessId, int oldRating, int newRating) {
        try {
            businessRepository.updateRatingAtomically(businessId, newRating - oldRating, 0);
        } catch (Exception e) {
            log.warn("Failed to adjust denormalized rating for business {}: {}", businessId, e.getMessage());
        }
    }
}
