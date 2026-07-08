package com.example.localityconnector.repository;

import com.example.localityconnector.model.PasswordResetToken;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class PasswordResetTokenFirestoreRepository extends BaseFirestoreRepository<PasswordResetToken> {

    public PasswordResetTokenFirestoreRepository(Firestore firestore) {
        super(firestore, "password_reset_tokens", PasswordResetToken.class);
    }

    public PasswordResetToken save(PasswordResetToken token) {
        if (token.getId() == null) {
            token.setId(newId());
        }
        await(collection().document(token.getId()).set(token), "Failed to save password reset token");
        return token;
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        return queryOne(collection().whereEqualTo("token", token),
                "Failed to fetch password reset token");
    }

    /** Delete expired tokens (cleanup). */
    public int deleteExpired() {
        if (collection() == null) return 0;
        List<PasswordResetToken> expired = queryList(
                collection().whereLessThan("expiresAt", new Date()),
                "Failed to fetch expired password reset tokens");
        for (PasswordResetToken token : expired) {
            deleteById(token.getId());
        }
        return expired.size();
    }
}
