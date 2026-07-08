package com.example.localityconnector.repository;

import com.example.localityconnector.model.BlacklistedToken;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Firestore-backed store for revoked JWTs (the {@code token_blacklist} collection).
 */
@Repository
public class JwtBlacklistFirestoreRepository extends BaseFirestoreRepository<BlacklistedToken> {

    private static final String COLLECTION_NAME = "token_blacklist";

    public JwtBlacklistFirestoreRepository(Firestore firestore) {
        super(firestore, COLLECTION_NAME, BlacklistedToken.class);
    }

    public BlacklistedToken save(BlacklistedToken token) {
        if (token.getId() == null) {
            token.setId(newId());
        }
        await(collection().document(token.getId()).set(token), "Failed to save blacklisted token");
        return token;
    }

    public boolean existsByJti(String jti) {
        return queryOne(collection().whereEqualTo("jti", jti).limit(1), "Failed to query token blacklist").isPresent();
    }

    /**
     * Delete entries whose original token has already expired.
     *
     * @return the number of deleted entries.
     */
    public int deleteExpired() {
        if (collection() == null) return 0;
        List<QueryDocumentSnapshot> documents = await(
                collection().whereLessThan("expiresAt", new Date()).get(),
                "Failed to clean up expired blacklist entries").getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            document.getReference().delete();
        }
        return documents.size();
    }
}
