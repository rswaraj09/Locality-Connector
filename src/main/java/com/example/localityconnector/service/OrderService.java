package com.example.localityconnector.service;

import com.example.localityconnector.model.Order;
import com.example.localityconnector.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    public Order createOrder(Order order) {
        order.prePersist();
        return orderRepository.save(order);
    }
    
    public List<Order> getOrdersByBusinessId(String businessId) {
        return orderRepository.findByBusinessId(businessId);
    }
    
    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public List<Order> getOrdersByBusinessIdAndStatus(String businessId, String status) {
        return orderRepository.findByBusinessIdAndStatus(businessId, status);
    }
    
    public List<Order> getOrdersByUserIdAndStatus(String userId, String status) {
        return orderRepository.findByUserIdAndStatus(userId, status);
    }
    
    public Optional<Order> findById(String id) {
        return orderRepository.findById(id);
    }
    
    public Order updateOrderStatus(String id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}

