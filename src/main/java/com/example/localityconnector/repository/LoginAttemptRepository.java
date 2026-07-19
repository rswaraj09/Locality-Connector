package com.example.localityconnector.repository;

import com.example.localityconnector.model.LoginAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends MongoRepository<LoginAttempt, String> {

    Optional<LoginAttempt> findByEmail(String email);

    void deleteByEmail(String email);
}
