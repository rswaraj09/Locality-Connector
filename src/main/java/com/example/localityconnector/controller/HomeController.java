package com.example.localityconnector.controller;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.dto.SaveLocationRequest;
import com.example.localityconnector.model.Item;
import com.example.localityconnector.repository.ItemRepository;
import com.example.localityconnector.repository.BusinessRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    private final BusinessRepository businessRepository;
    private final ItemRepository itemRepository;

    public HomeController(BusinessRepository businessRepository, ItemRepository itemRepository) {
        this.businessRepository = businessRepository;
        this.itemRepository = itemRepository;
    }

    // Main pages
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Authentication pages
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    // User pages
    @GetMapping("/user-home")
    public String userHome() {
        return "user_home";
    }

    @GetMapping("/user-homepage")
    public String userHomepage() {
        return "user_homepage";
    }

    // Business pages
    @GetMapping("/business-dashboard")
    public String businessDashboard() {
        return "business_dashboard";
    }

    @GetMapping("/update-business")
    public String updateBusiness() {
        return "update-business";
    }

    // Shop and listing pages
    @GetMapping("/listing")
    public String listing() {
        return "listing";
    }

    @GetMapping("/addlisting")
    public String addListingPage() {
        return "addlisting";
    }

    // Save business location (expects businessName, businessLatitude, businessLongitude)
    @PostMapping(value = "/saveLocation", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public java.util.Map<String, Object> saveLocation(@RequestBody(required = true) SaveLocationRequest request, jakarta.servlet.http.HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            String name = request.getBusinessName();
            if (name == null || name.trim().isEmpty() || name.contains("${sessionScope")) {
                Object sessionName = session.getAttribute("loggedInBusinessName");
                if (sessionName != null) name = sessionName.toString();
            }
            if (name == null || name.isBlank()) {
                response.put("error", "Business name is missing and no session value found");
                return response;
            }
            Business business = businessRepository.findByBusinessName(name)
                    .orElseThrow(() -> new RuntimeException("Business not found: " + request.getBusinessName()));
            business.setLatitude(request.getBusinessLatitude());
            business.setLongitude(request.getBusinessLongitude());
            business.setUpdatedAt(java.time.LocalDateTime.now());
            businessRepository.save(business);
            response.put("success", "Location saved successfully");
        } catch (Exception ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }

    // Bridge endpoint to create an Item using the new API, keeping existing UI calls working
    @PostMapping(value = "/addListing", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> addListing(@RequestBody(required = true) java.util.Map<String, Object> payload,
                                                                 jakarta.servlet.http.HttpSession session) {
        // Delegate to /api/items for real persistence
        try {
            String reqBusinessName = (String) payload.get("businessName");
            String resolvedBusinessName = reqBusinessName;
            if (resolvedBusinessName == null || resolvedBusinessName.isBlank() || resolvedBusinessName.contains("${sessionScope")) {
                Object n = session.getAttribute("loggedInBusinessName");
                if (n != null) resolvedBusinessName = n.toString();
            }
            if (resolvedBusinessName == null || resolvedBusinessName.isBlank()) {
                return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", "Business name required"));
            }
            String itemName = (String) payload.get("itemName");
            Object priceObj = payload.get("itemPrice");
            Double price = priceObj instanceof Number ? ((Number) priceObj).doubleValue() : Double.parseDouble(String.valueOf(priceObj));
            String description = payload.get("itemDescription") == null ? "" : String.valueOf(payload.get("itemDescription"));

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
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("success", "Item saved", "itemId", item.getId()));
        } catch (Exception ex) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/shop-details")
    public String shopDetails() {
        return "shop_details";
    }

    @GetMapping("/details")
    public String details() {
        return "details";
    }

    // Category pages
    @GetMapping("/food")
    public String food() {
        return "food";
    }

    @GetMapping("/fooditem")
    public String fooditem() {
        return "fooditem";
    }

    @GetMapping("/medicine")
    public String medicine() {
        return "medicine";
    }

    @GetMapping("/pharmacy")
    public String pharmacy() {
        return "pharmacy";
    }

    @GetMapping("/clothing")
    public String clothing() {
        return "clothing";
    }

    @GetMapping("/stationary")
    public String stationary() {
        return "stationary";
    }

    @GetMapping("/hospitals")
    public String hospitals() {
        return "hospitals";
    }

    @GetMapping("/hospital-homepage")
    public String hospitalHomepage() {
        return "hospitalhomepage";
    }

    // Nearby pages
    @GetMapping("/nearby-shops")
    public String nearbyShops() {
        return "nearbyshops";
    }

    @GetMapping("/nearby-food")
    public String nearbyFood() {
        return "nearbyfooditems";
    }

    @GetMapping("/nearby-pharmacy")
    public String nearbyPharmacy() {
        return "nearbypharmacy";
    }

    @GetMapping("/nearby-clothing")
    public String nearbyClothing() {
        return "nearbyclothing";
    }

    @GetMapping("/nearby-stationary")
    public String nearbyStationary() {
        return "nearbystationary";
    }

    @GetMapping("/nearby-hospital")
    public String nearbyHospital() {
        return "nearbyhospital";
    }

    @GetMapping("/nearby-grocery")
    public String nearbyGrocery() {
        return "nearby_grocery";
    }

    // Order and payment pages
    @GetMapping("/order")
    public String order() {
        return "order";
    }

    @GetMapping("/orders")
    public String orders() {
        return "orders";
    }

    @GetMapping("/order-placed")
    public String orderPlaced() {
        return "orderplaced";
    }

    @GetMapping("/payment")
    public String payment() {
        return "payment";
    }

    // Utility pages
    @GetMapping("/grocery-list")
    public String groceryList() {
        return "grocerylist";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "feedback";
    }

    @GetMapping("/test-api")
    public String testApi() {
        return "test-api";
    }
}


