package com.example.localityconnector.service;

import com.example.localityconnector.model.Notification;
import com.example.localityconnector.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for creating, reading, and managing in-app notifications.
 * Triggered by business events (feedback, verification, moderation).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    private static final Sort CREATED_DESC = Sort.by(Sort.Direction.DESC, "createdAt");

    /**
     * Create and persist a notification.
     */
    public Notification create(String recipientId, String recipientType,
                               String title, String message, String type, String referenceId) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRecipientType(recipientType);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.prePersist();
        Notification saved = notificationRepository.save(notification);
        if (messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSendToUser(recipientId, "/queue/notifications", saved);
            } catch (Exception e) {
                log.warn("Failed to send WebSocket notification to user {}: {}", recipientId, e.getMessage());
            }
        }
        return saved;
    }

    public List<Notification> getNotifications(String recipientId) {
        return notificationRepository.findByRecipientId(recipientId, CREATED_DESC);
    }

    public List<Notification> getUnreadNotifications(String recipientId) {
        return notificationRepository.findByRecipientIdAndReadFalse(recipientId, CREATED_DESC);
    }

    public long getUnreadCount(String recipientId) {
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    public void markAsRead(String notificationId) {
        Optional<Notification> opt = notificationRepository.findById(notificationId);
        opt.ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public int markAllAsRead(String recipientId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadFalse(recipientId, CREATED_DESC);
        int count = 0;
        for (Notification n : unread) {
            n.setRead(true);
            notificationRepository.save(n);
            count++;
        }
        return count;
    }

    public boolean deleteNotification(String notificationId, String recipientId) {
        Optional<Notification> opt = notificationRepository.findById(notificationId);
        if (opt.isPresent() && recipientId.equals(opt.get().getRecipientId())) {
            notificationRepository.deleteById(notificationId);
            return true;
        }
        return false;
    }

    public int deleteAllNotifications(String recipientId) {
        List<Notification> all = notificationRepository.findByRecipientId(recipientId, CREATED_DESC);
        int count = all.size();
        notificationRepository.deleteAll(all);
        return count;
    }

    // --- Convenience trigger methods ---

    public void notifyFeedbackReceived(String businessId, String businessName,
                                        String userName, int rating) {
        create(businessId, "BUSINESS",
                "New Review Received",
                userName + " rated your business " + rating + "/5 stars.",
                "FEEDBACK_RECEIVED", null);
    }

    public void notifyBusinessVerified(String businessId, String businessName) {
        create(businessId, "BUSINESS",
                "Business Verified",
                "Congratulations! " + businessName + " has been verified.",
                "BUSINESS_VERIFIED", businessId);
    }

    public void notifyBusinessSuspended(String businessId, String businessName) {
        create(businessId, "BUSINESS",
                "Business Suspended",
                businessName + " has been suspended. Contact support for details.",
                "BUSINESS_SUSPENDED", businessId);
    }

    public void notifyReviewFlagged(String userId, String feedbackId) {
        create(userId, "USER",
                "Review Flagged",
                "One of your reviews has been flagged for moderation.",
                "REVIEW_FLAGGED", feedbackId);
    }

    public void notifyReviewRemoved(String userId, String feedbackId) {
        create(userId, "USER",
                "Review Removed",
                "One of your reviews has been removed by moderation.",
                "REVIEW_REMOVED", feedbackId);
    }
}
