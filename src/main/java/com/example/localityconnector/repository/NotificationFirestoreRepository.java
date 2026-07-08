package com.example.localityconnector.repository;

import com.example.localityconnector.model.Notification;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationFirestoreRepository extends BaseFirestoreRepository<Notification> {

    public NotificationFirestoreRepository(Firestore firestore) {
        super(firestore, "notifications", Notification.class);
    }

    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            notification.setId(newId());
        }
        await(collection().document(notification.getId()).set(notification), "Failed to save notification");
        return notification;
    }

    public List<Notification> findByRecipientId(String recipientId) {
        return queryList(collection()
                        .whereEqualTo("recipientId", recipientId)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .limit(50),
                "Failed to fetch notifications");
    }

    public List<Notification> findUnreadByRecipientId(String recipientId) {
        return queryList(collection()
                        .whereEqualTo("recipientId", recipientId)
                        .whereEqualTo("read", false)
                        .orderBy("createdAt", Query.Direction.DESCENDING),
                "Failed to fetch unread notifications");
    }

    public long countUnreadByRecipientId(String recipientId) {
        return countQuery(collection()
                        .whereEqualTo("recipientId", recipientId)
                        .whereEqualTo("read", false),
                "Failed to count unread notifications");
    }

    /** Mark all notifications for a recipient as read. */
    public int markAllRead(String recipientId) {
        List<Notification> unread = findUnreadByRecipientId(recipientId);
        var batch = firestore.batch();
        int count = 0;
        for (Notification notification : unread) {
            batch.update(collection().document(notification.getId()), "read", true);
            count++;
        }
        if (count > 0) {
            await(batch.commit(), "Failed to mark notifications as read");
        }
        return count;
    }

    /** Delete all notifications for a recipient. */
    public int deleteAllByRecipientId(String recipientId) {
        List<Notification> all = findByRecipientId(recipientId);
        var batch = firestore.batch();
        int count = 0;
        for (Notification notification : all) {
            batch.delete(collection().document(notification.getId()));
            count++;
        }
        if (count > 0) {
            await(batch.commit(), "Failed to delete notifications");
        }
        return count;
    }
}
