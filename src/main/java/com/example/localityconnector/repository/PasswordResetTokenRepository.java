package com.example.localityconnector.repository;

import com.example.localityconnector.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByToken(String token);

    /** Delete expired tokens (cleanup). */
    long deleteByExpiresAtBefore(Date date);
}
