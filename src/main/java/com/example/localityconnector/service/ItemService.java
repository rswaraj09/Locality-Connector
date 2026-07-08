package com.example.localityconnector.service;

import com.example.localityconnector.exception.ResourceNotFoundException;
import com.example.localityconnector.model.Item;
import com.example.localityconnector.repository.ItemFirestoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemFirestoreRepository itemRepository;

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
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        item.setName(itemDetails.getName());
        item.setPrice(itemDetails.getPrice());
        item.setDescription(itemDetails.getDescription());
        item.prePersist();

        return itemRepository.save(item);
    }

    /** Propagate a business rename to all of its catalog items. */
    public int updateBusinessNameOnItems(String businessId, String newBusinessName) {
        return itemRepository.updateBusinessNameOnItems(businessId, newBusinessName);
    }

    public void deleteItem(String id) {
        itemRepository.deleteById(id);
    }

    /**
     * Cascade-delete every item owned by a business in a single batched round-trip.
     *
     * @return the number of items removed.
     */
    public int deleteItemsByBusinessId(String businessId) {
        return itemRepository.deleteByBusinessId(businessId);
    }
}
