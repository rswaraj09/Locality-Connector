package com.example.localityconnector.repository;

import com.example.localityconnector.model.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends MongoRepository<VerificationToken, String> {

    Optional<VerificationToken> findByToken(String token);

    /** Delete expired tokens (cleanup). */
    long deleteByExpiresAtBefore(Date date);
}
