package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.model.Item;
import com.example.localityconnector.repository.BusinessRepository;
import com.example.localityconnector.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

	private final ItemRepository itemRepository;
	private final BusinessRepository businessRepository;

	@PostMapping
	public ResponseEntity<?> create(@RequestBody Map<String, Object> body, jakarta.servlet.http.HttpSession session) {
		try {
			// Get businessId from session (more secure than using business name)
			String businessId = (String) session.getAttribute("loggedInBusinessId");
			if (businessId == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "Business not logged in"));
			}

			String itemName = (String) body.get("itemName");
			Object priceObj = body.get("itemPrice");
			Double price = priceObj instanceof Number ? ((Number) priceObj).doubleValue() : Double.parseDouble(String.valueOf(priceObj));
			String description = body.get("itemDescription") == null ? "" : String.valueOf(body.get("itemDescription"));

			// Get business details using businessId
			Business business = businessRepository.findById(businessId)
					.orElseThrow(() -> new RuntimeException("Business not found with ID: " + businessId));

			Item item = new Item();
			item.setBusinessId(business.getId()); // Foreign key relationship
			item.setBusinessName(business.getBusinessName());
			item.setName(itemName);
			item.setPrice(price);
			item.setDescription(description);
			item.prePersist();
			itemRepository.save(item);
			return ResponseEntity.ok(Map.of("success", "Item saved", "itemId", item.getId()));
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@GetMapping
    public ResponseEntity<?> list(jakarta.servlet.http.HttpSession session) {
		try {
			// Get businessId from session (more secure than using business name)
			String businessId = (String) session.getAttribute("loggedInBusinessId");
			if (businessId == null) {
				return ResponseEntity.badRequest().body(Map.of("error", "Business not logged in"));
			}

			// Get items using businessId (foreign key relationship)
			List<Item> items = itemRepository.findByBusinessId(businessId);
			return ResponseEntity.ok(items);
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
		return itemRepository.findById(id)
				.map(existing -> {
					if (body.containsKey("itemName")) {
						existing.setName(String.valueOf(body.get("itemName")));
					}
					if (body.containsKey("itemPrice")) {
						Object p = body.get("itemPrice");
						double price = p instanceof Number ? ((Number) p).doubleValue() : Double.parseDouble(String.valueOf(p));
						existing.setPrice(price);
					}
					if (body.containsKey("itemDescription")) {
						existing.setDescription(body.get("itemDescription") == null ? "" : String.valueOf(body.get("itemDescription")));
					}
					existing.setUpdatedAt(java.time.LocalDateTime.now());
					itemRepository.save(existing);
					return ResponseEntity.ok(Map.of("success", "Item updated"));
				})
				.orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Item not found")));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable String id) {
		if (!itemRepository.existsById(id)) {
			return ResponseEntity.badRequest().body(Map.of("error", "Item not found"));
		}
		itemRepository.deleteById(id);
		return ResponseEntity.ok(Map.of("success", "Item deleted"));
	}

	@GetMapping("/business/{businessId}")
	public ResponseEntity<?> getItemsByBusinessId(@PathVariable String businessId) {
		try {
			List<Item> items = itemRepository.findByBusinessId(businessId);
			return ResponseEntity.ok(items);
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		}
	}
}






