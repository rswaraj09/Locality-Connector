package com.example.localityconnector.repository;

import com.example.localityconnector.model.BlacklistedToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface JwtBlacklistRepository extends MongoRepository<BlacklistedToken, String> {

    boolean existsByJti(String jti);

    /** Delete entries whose original token has already expired. */
    long deleteByExpiresAtBefore(Date date);
}
