package com.example.localityconnector.service;

import com.example.localityconnector.model.Item;
import com.example.localityconnector.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    
    private final ItemRepository itemRepository;
    
    public Item createItem(Item item) {
        item.prePersist();
        return itemRepository.save(item);
    }
    
    public List<Item> getItemsByBusinessId(String businessId) {
        return itemRepository.findByBusinessId(businessId);
    }
    
    public List<Item> getItemsByBusinessName(String businessName) {
        return itemRepository.findByBusinessName(businessName);
    }
    
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    
    public Optional<Item> findById(String id) {
        return itemRepository.findById(id);
    }
    
    public Item updateItem(String id, Item itemDetails) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        item.setName(itemDetails.getName());
        item.setPrice(itemDetails.getPrice());
        item.setDescription(itemDetails.getDescription());
        item.setUpdatedAt(java.time.LocalDateTime.now());
        
        return itemRepository.save(item);
    }
    
    public void deleteItem(String id) {
        itemRepository.deleteById(id);
    }
}

