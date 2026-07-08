package com.example.localityconnector.repository;

import com.example.localityconnector.model.Item;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemFirestoreRepository extends BaseFirestoreRepository<Item> {

    private static final String COLLECTION_NAME = "items";

    /** Firestore caps a single WriteBatch at 500 operations; stay safely below that. */
    private static final int BATCH_LIMIT = 450;

    public ItemFirestoreRepository(Firestore firestore) {
        super(firestore, COLLECTION_NAME, Item.class);
    }

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(newId());
        }
        await(collection().document(item.getId()).set(item), "Failed to save item");
        return item;
    }

    public List<Item> findByBusinessId(String businessId) {
        return queryList(collection().whereEqualTo("businessId", businessId),
                "Failed to fetch items by businessId");
    }

    public List<Item> findByBusinessName(String businessName) {
        return queryList(collection().whereEqualTo("businessName", businessName),
                "Failed to fetch items by businessName");
    }

    public Optional<Item> findByIdOptional(String id) {
        return findById(id);
    }

    /**
     * Keep the denormalised {@code businessName} on every item in sync when a business
     * renames itself, committing the updates in batched single round-trips.
     *
     * @return the number of items updated
     */
    public int updateBusinessNameOnItems(String businessId, String newBusinessName) {
        List<QueryDocumentSnapshot> documents = await(
                collection().whereEqualTo("businessId", businessId).get(),
                "Failed to fetch items for rename").getDocuments();
        Date now = new Date();
        int updated = 0;
        int index = 0;
        while (index < documents.size()) {
            WriteBatch batch = firestore.batch();
            int end = Math.min(index + BATCH_LIMIT, documents.size());
            for (; index < end; index++) {
                batch.update(documents.get(index).getReference(),
                        "businessName", newBusinessName, "updatedAt", now);
                updated++;
            }
            await(batch.commit(), "Failed to commit item businessName update batch");
        }
        return updated;
    }

    /**
     * Cascade-delete every item owned by a business using batched commits.
     *
     * @return the number of items removed
     */
    public int deleteByBusinessId(String businessId) {
        List<QueryDocumentSnapshot> documents = await(
                collection().whereEqualTo("businessId", businessId).get(),
                "Failed to fetch items for delete").getDocuments();
        int removed = 0;
        int index = 0;
        while (index < documents.size()) {
            WriteBatch batch = firestore.batch();
            int end = Math.min(index + BATCH_LIMIT, documents.size());
            for (; index < end; index++) {
                batch.delete(documents.get(index).getReference());
                removed++;
            }
            await(batch.commit(), "Failed to commit item delete batch");
        }
        return removed;
    }
}
