package com.example.localityconnector.repository;

import com.example.localityconnector.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {

    Optional<OtpToken> findTopByTargetAndTargetTypeOrderByCreatedAtDesc(String target, String targetType);

    Optional<OtpToken> findTopByTargetAndOtpCodeOrderByCreatedAtDesc(String target, String otpCode);

    long deleteByExpiresAtBefore(Date date);
}
