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
			String reqBusinessName = (String) body.get("businessName");
			String resolvedBusinessName = reqBusinessName;
			if (resolvedBusinessName == null || resolvedBusinessName.isBlank() || resolvedBusinessName.contains("${sessionScope")) {
				Object n = session.getAttribute("loggedInBusinessName");
				if (n != null) resolvedBusinessName = n.toString();
			}
			String itemName = (String) body.get("itemName");
			Object priceObj = body.get("itemPrice");
			Double price = priceObj instanceof Number ? ((Number) priceObj).doubleValue() : Double.parseDouble(String.valueOf(priceObj));
			String description = body.get("itemDescription") == null ? "" : String.valueOf(body.get("itemDescription"));

			final String bn = resolvedBusinessName;
			Business business = businessRepository.findByBusinessName(bn)
					.orElseThrow(() -> new RuntimeException("Business not found: " + bn));

			Item item = new Item();
			item.setBusinessId(business.getId());
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
    public ResponseEntity<?> list(@RequestParam(required = false) String businessName,
                                    jakarta.servlet.http.HttpSession session) {
		try {
            String resolvedBusinessName = businessName;
            if (resolvedBusinessName == null || resolvedBusinessName.isBlank() || resolvedBusinessName.contains("${sessionScope")) {
                Object n = session.getAttribute("loggedInBusinessName");
                if (n != null) resolvedBusinessName = n.toString();
            }
            if (resolvedBusinessName == null || resolvedBusinessName.isBlank()) {
				return ResponseEntity.badRequest().body(Map.of("error", "businessName required"));
			}
            final String bn = resolvedBusinessName;
            Business business = businessRepository.findByBusinessName(bn)
                    .orElseThrow(() -> new RuntimeException("Business not found: " + bn));
			List<Item> items = itemRepository.findByBusinessId(business.getId());
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
}






