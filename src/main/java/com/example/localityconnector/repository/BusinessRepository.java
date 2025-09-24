package com.example.localityconnector.repository;

import com.example.localityconnector.model.Business;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends MongoRepository<Business, String> {
    
    Optional<Business> findByEmail(String email);
    
    Optional<Business> findByBusinessName(String businessName);
    
    boolean existsByEmail(String email);
    
    boolean existsByBusinessName(String businessName);
    
    Optional<Business> findByEmailAndPassword(String email, String password);
    
    List<Business> findByCategory(String category);
    
    List<Business> findByCategoryAndIsActiveTrue(String category);
}



















