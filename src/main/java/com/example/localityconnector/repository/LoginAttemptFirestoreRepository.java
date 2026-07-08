package com.example.localityconnector.repository;

import com.example.localityconnector.model.LoginAttempt;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Firestore-backed store for brute-force login throttling state ({@code login_attempts}).
 * The document id is the lowercased email so reads/writes are single-document lookups.
 */
@Repository
public class LoginAttemptFirestoreRepository extends BaseFirestoreRepository<LoginAttempt> {

    private static final String COLLECTION_NAME = "login_attempts";

    public LoginAttemptFirestoreRepository(Firestore firestore) {
        super(firestore, COLLECTION_NAME, LoginAttempt.class);
    }

    public Optional<LoginAttempt> findByKey(String key) {
        return findById(key);
    }

    public void save(String key, LoginAttempt attempt) {
        attempt.setId(key);
        await(collection().document(key).set(attempt), "Failed to save login attempt record");
    }

    public void deleteByKey(String key) {
        deleteById(key);
    }

    public void recordFailedAttemptAtomically(String key, long windowMillis, int maxAttempts, long lockMillis) {
        await(firestore.runTransaction(transaction -> {
            com.google.cloud.firestore.DocumentReference docRef = collection().document(key);
            com.google.cloud.firestore.DocumentSnapshot snapshot = transaction.get(docRef).get();
            long now = System.currentTimeMillis();
            LoginAttempt record = null;
            if (snapshot.exists()) {
                record = snapshot.toObject(LoginAttempt.class);
            }
            if (record == null) {
                record = new LoginAttempt();
                record.setId(key);
                record.setEmail(key);
                record.setCount(0);
                record.setFirstAttemptEpochMs(now);
            }

            if (record.getFirstAttemptEpochMs() == null
                    || (now - record.getFirstAttemptEpochMs()) >= windowMillis) {
                record.setFirstAttemptEpochMs(now);
                record.setCount(0);
                record.setLockedUntilEpochMs(null);
            }
            record.setCount(record.getCount() + 1);
            if (record.getCount() >= maxAttempts) {
                record.setLockedUntilEpochMs(now + lockMillis);
            }
            transaction.set(docRef, record);
            return null;
        }), "Failed to record login attempt atomically");
    }
}
