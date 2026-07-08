package com.example.localityconnector.repository;

import com.example.localityconnector.model.User;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserFirestoreRepository extends BaseFirestoreRepository<User> {

    private static final String COLLECTION_NAME = "users";

    public UserFirestoreRepository(Firestore firestore) {
        super(firestore, COLLECTION_NAME, User.class);
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(newId());
        }
        await(collection().document(user.getId()).set(user), "Failed to save user");
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return queryOne(collection().whereEqualTo("email", email), "Failed to fetch user by email");
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
