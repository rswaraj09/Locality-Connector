package com.example.localityconnector.service;

import com.example.localityconnector.model.Feedback;
import com.example.localityconnector.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    
    public Feedback createFeedback(Feedback feedback) {
        feedback.prePersist();
        return feedbackRepository.save(feedback);
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
}

