package com.example.localityconnector.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Generic base class for the Firestore-backed repositories.
 *
 * <p>Centralises the {@code ApiFuture.get()} / {@code try-catch} / {@code RuntimeException}
 * boilerplate that was previously duplicated across every repository, and holds the
 * constructor-injected {@link Firestore} bean (provided by {@code FirebaseConfig}) so that
 * concrete repositories no longer call {@code FirestoreClient.getFirestore()} statically and
 * can be unit-tested with a mocked {@link Firestore}.</p>
 *
 * @param <T> the model type stored in this collection
 */
public abstract class BaseFirestoreRepository<T> {

    protected final Firestore firestore;
    protected final String collectionName;
    private final Class<T> type;

    protected BaseFirestoreRepository(Firestore firestore, String collectionName, Class<T> type) {
        this.firestore = firestore;
        this.collectionName = collectionName;
        this.type = type;
    }

    protected CollectionReference collection() {
        return firestore.collection(collectionName);
    }

    /** Generate a new Firestore document id for this collection. */
    protected String newId() {
        return collection().document().getId();
    }

    /**
     * Block on an {@link ApiFuture}, translating the checked Firestore exceptions into an
     * unchecked {@link RuntimeException} so callers don't repeat the same boilerplate.
     */
    protected <R> R await(ApiFuture<R> future, String errorMessage) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(errorMessage, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    protected List<T> toList(QuerySnapshot snapshot) {
        List<T> results = new ArrayList<>();
        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            results.add(document.toObject(type));
        }
        return results;
    }

    /** Run a query and map every document to {@code T}. */
    protected List<T> queryList(Query query, String errorMessage) {
        return toList(await(query.get(), errorMessage));
    }

    /** Run a query and return the first matching document, if any. */
    protected Optional<T> queryOne(Query query, String errorMessage) {
        List<QueryDocumentSnapshot> documents = await(query.get(), errorMessage).getDocuments();
        if (documents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(documents.get(0).toObject(type));
    }

    /** Server-side aggregation count (no document reads). */
    protected long countQuery(Query query, String errorMessage) {
        try {
            return query.count().get().get().getCount();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(errorMessage, e);
        } catch (ExecutionException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    public Optional<T> findById(String id) {
        DocumentSnapshot document = await(collection().document(id).get(),
                "Failed to fetch document from " + collectionName);
        if (document.exists()) {
            return Optional.ofNullable(document.toObject(type));
        }
        return Optional.empty();
    }

    public List<T> findAll() {
        return queryList(collection(), "Failed to fetch all documents from " + collectionName);
    }

    public void deleteById(String id) {
        collection().document(id).delete();
    }
}
