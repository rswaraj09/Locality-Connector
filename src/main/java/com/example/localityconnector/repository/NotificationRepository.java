package com.example.localityconnector.repository;

import com.example.localityconnector.model.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByRecipientId(String recipientId, Sort sort);

    List<Notification> findByRecipientIdAndReadFalse(String recipientId, Sort sort);

    long countByRecipientIdAndReadFalse(String recipientId);

    void deleteByRecipientId(String recipientId);
}
