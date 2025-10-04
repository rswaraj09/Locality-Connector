package com.example.localityconnector.repository;

import com.example.localityconnector.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByBusinessId(String businessId);
    List<Order> findByUserId(String userId);
    List<Order> findByBusinessIdAndStatus(String businessId, String status);
    List<Order> findByUserIdAndStatus(String userId, String status);
}

