package com.example.localityconnector.repository;

import com.example.localityconnector.model.VerificationToken;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class VerificationTokenFirestoreRepository extends BaseFirestoreRepository<VerificationToken> {

    public VerificationTokenFirestoreRepository(Firestore firestore) {
        super(firestore, "verification_tokens", VerificationToken.class);
    }

    public VerificationToken save(VerificationToken token) {
        if (token.getId() == null) {
            token.setId(newId());
        }
        await(collection().document(token.getId()).set(token), "Failed to save verification token");
        return token;
    }

    public Optional<VerificationToken> findByToken(String token) {
        return queryOne(collection().whereEqualTo("token", token),
                "Failed to fetch verification token");
    }

    /** Delete expired tokens (cleanup). */
    public int deleteExpired() {
        if (collection() == null) return 0;
        List<VerificationToken> expired = queryList(
                collection().whereLessThan("expiresAt", new Date()),
                "Failed to fetch expired verification tokens");
        for (VerificationToken token : expired) {
            deleteById(token.getId());
        }
        return expired.size();
    }
}
