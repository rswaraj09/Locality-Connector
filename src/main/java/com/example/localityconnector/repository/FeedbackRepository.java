package com.example.localityconnector.repository;

import com.example.localityconnector.model.Feedback;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {

    List<Feedback> findByBusinessId(String businessId);

    List<Feedback> findByBusinessId(String businessId, Sort sort);

    List<Feedback> findByUserId(String userId);
}
