package com.example.localityconnector.service;

import com.example.localityconnector.model.Business;
import com.example.localityconnector.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSeedingService implements CommandLineRunner {
    
    private final BusinessRepository businessRepository;
    private final GooglePlacesService googlePlacesService;
    
    @Value("${app.seed.data:false}")
    private boolean shouldSeedData;
    
    @Value("${app.seed.budget.mode:true}")
    private boolean budgetMode;
    
    // Comprehensive Indian coverage including villages and rural areas
    private static final List<StateLocation> STATES = Arrays.asList(
        // Maharashtra - 50+ cities, towns, and villages
        new StateLocation("Maharashtra", Arrays.asList(
            // Major Cities
            new CityLocation("Mumbai", 19.0760, 72.8777),
            new CityLocation("Pune", 18.5204, 73.8567),
            new CityLocation("Nagpur", 21.1458, 79.0882),
            new CityLocation("Nashik", 19.9975, 73.7898),
            new CityLocation("Aurangabad", 19.8762, 75.3433),
            new CityLocation("Solapur", 17.6599, 75.9064),
            new CityLocation("Amravati", 20.9374, 77.7796),
            new CityLocation("Kolhapur", 16.7050, 74.2433),
            new CityLocation("Sangli", 16.8524, 74.5815),
            new CityLocation("Malegaon", 20.5598, 74.5251),
            new CityLocation("Jalgaon", 21.0077, 75.5626),
            new CityLocation("Akola", 20.7000, 77.0000),
            new CityLocation("Latur", 18.4088, 76.5604),
            new CityLocation("Dhule", 20.9028, 74.7774),
            new CityLocation("Chandrapur", 19.9615, 79.2961),
            new CityLocation("Parbhani", 19.2606, 76.7794),
            new CityLocation("Ichalkaranji", 16.7000, 74.4667),
            new CityLocation("Jalna", 19.8413, 75.8864),
            // Additional Cities and Towns
            new CityLocation("Bhiwandi", 19.3002, 73.0589),
            new CityLocation("Ulhasnagar", 19.2167, 73.1500),
            new CityLocation("Kalyan", 19.2403, 73.1305),
            new CityLocation("Dombivli", 19.2167, 73.0833),
            new CityLocation("Thane", 19.2183, 72.9781),
            new CityLocation("Mira-Bhayandar", 19.2952, 72.8544),
            new CityLocation("Vasai-Virar", 19.4700, 72.8000),
            new CityLocation("Navi Mumbai", 19.0330, 73.0297),
            new CityLocation("Panvel", 18.9881, 73.1102),
            new CityLocation("Badlapur", 19.1500, 73.2667),
            new CityLocation("Ambernath", 19.2086, 73.1833),
            new CityLocation("Ulhasnagar", 19.2167, 73.1500),
            new CityLocation("Ambarnath", 19.2086, 73.1833),
            new CityLocation("Bhiwandi", 19.3002, 73.0589),
            new CityLocation("Shahapur", 19.4500, 73.3333),
            new CityLocation("Vasai", 19.4700, 72.8000),
            new CityLocation("Virar", 19.4700, 72.8000),
            new CityLocation("Nalasopara", 19.4167, 72.7667),
            new CityLocation("Boisar", 19.8000, 72.7500),
            new CityLocation("Palghar", 19.7000, 72.7500),
            new CityLocation("Dahanu", 19.9833, 72.7333),
            new CityLocation("Talasari", 20.0167, 72.7167),
            new CityLocation("Jawhar", 19.9167, 73.2333),
            new CityLocation("Mokhada", 19.9333, 73.3167),
            new CityLocation("Vikramgad", 19.8500, 73.3167),
            new CityLocation("Wada", 19.6500, 73.1333),
            new CityLocation("Vada", 19.6500, 73.1333),
            new CityLocation("Bhiwandi", 19.3002, 73.0589),
            new CityLocation("Kalyan", 19.2403, 73.1305),
            new CityLocation("Dombivli", 19.2167, 73.0833),
            new CityLocation("Ambernath", 19.2086, 73.1833),
            new CityLocation("Badlapur", 19.1500, 73.2667),
            new CityLocation("Ulhasnagar", 19.2167, 73.1500),
            new CityLocation("Bhiwandi", 19.3002, 73.0589),
            new CityLocation("Kalyan", 19.2403, 73.1305),
            new CityLocation("Dombivli", 19.2167, 73.0833),
            new CityLocation("Ambernath", 19.2086, 73.1833),
            new CityLocation("Badlapur", 19.1500, 73.2667)
        )),
        // Delhi NCR - 8+ cities
        new StateLocation("Delhi NCR", Arrays.asList(
            new CityLocation("New Delhi", 28.7041, 77.1025),
            new CityLocation("Gurgaon", 28.4595, 77.0266),
            new CityLocation("Noida", 28.5355, 77.3910),
            new CityLocation("Faridabad", 28.4089, 77.3178),
            new CityLocation("Ghaziabad", 28.6692, 77.4538),
            new CityLocation("Greater Noida", 28.4744, 77.5040),
            new CityLocation("Sonipat", 28.9931, 77.0151),
            new CityLocation("Bahadurgarh", 28.6928, 76.9356)
        )),
        // Karnataka - 12+ major cities
        new StateLocation("Karnataka", Arrays.asList(
            new CityLocation("Bangalore", 12.9716, 77.5946),
            new CityLocation("Mysore", 12.2958, 76.6394),
            new CityLocation("Mangalore", 12.9141, 74.8560),
            new CityLocation("Hubli", 15.3647, 75.1240),
            new CityLocation("Belgaum", 15.8497, 74.4977),
            new CityLocation("Gulbarga", 17.3297, 76.8343),
            new CityLocation("Davanagere", 14.4644, 75.9218),
            new CityLocation("Bellary", 15.1394, 76.9214),
            new CityLocation("Bijapur", 16.8244, 75.7154),
            new CityLocation("Shimoga", 13.9299, 75.5681),
            new CityLocation("Tumkur", 13.3399, 77.1013),
            new CityLocation("Raichur", 16.2076, 77.3463)
        )),
        // Tamil Nadu - 15+ major cities
        new StateLocation("Tamil Nadu", Arrays.asList(
            new CityLocation("Chennai", 13.0827, 80.2707),
            new CityLocation("Coimbatore", 11.0168, 76.9558),
            new CityLocation("Madurai", 9.9252, 78.1198),
            new CityLocation("Tiruchirapalli", 10.7905, 78.7047),
            new CityLocation("Salem", 11.6643, 78.1460),
            new CityLocation("Tirunelveli", 8.7139, 77.7567),
            new CityLocation("Tiruppur", 11.1085, 77.3411),
            new CityLocation("Erode", 11.3410, 77.7172),
            new CityLocation("Vellore", 12.9202, 79.1500),
            new CityLocation("Thoothukudi", 8.7642, 78.1348),
            new CityLocation("Dindigul", 10.3450, 77.9606),
            new CityLocation("Thanjavur", 10.7869, 79.1378),
            new CityLocation("Ranipet", 12.9295, 79.3314),
            new CityLocation("Sivakasi", 9.4491, 77.7976),
            new CityLocation("Karur", 10.9571, 78.0809)
        )),
        // Telangana - 8+ major cities
        new StateLocation("Telangana", Arrays.asList(
            new CityLocation("Hyderabad", 17.3850, 78.4867),
            new CityLocation("Warangal", 17.9689, 79.5941),
            new CityLocation("Nizamabad", 18.6711, 78.0938),
            new CityLocation("Khammam", 17.2473, 80.1514),
            new CityLocation("Karimnagar", 18.4386, 79.1288),
            new CityLocation("Ramagundam", 18.8000, 79.4500),
            new CityLocation("Mahabubnagar", 16.7402, 77.9903),
            new CityLocation("Nalgonda", 17.0544, 79.2671)
        )),
        // West Bengal - 10+ major cities
        new StateLocation("West Bengal", Arrays.asList(
            new CityLocation("Kolkata", 22.5726, 88.3639),
            new CityLocation("Asansol", 23.6889, 86.9661),
            new CityLocation("Siliguri", 26.7271, 88.3953),
            new CityLocation("Durgapur", 23.5204, 87.3119),
            new CityLocation("Bardhaman", 23.2400, 87.8694),
            new CityLocation("Malda", 25.0116, 88.1453),
            new CityLocation("Bahrampur", 24.1000, 88.2500),
            new CityLocation("Habra", 22.8300, 88.6300),
            new CityLocation("Kharagpur", 22.3302, 87.3237),
            new CityLocation("Shantipur", 23.2500, 88.4300)
        )),
        // Gujarat - 12+ major cities
        new StateLocation("Gujarat", Arrays.asList(
            new CityLocation("Ahmedabad", 23.0225, 72.5714),
            new CityLocation("Surat", 21.1702, 72.8311),
            new CityLocation("Vadodara", 22.3072, 73.1812),
            new CityLocation("Rajkot", 22.3039, 70.8022),
            new CityLocation("Bhavnagar", 21.7645, 72.1519),
            new CityLocation("Jamnagar", 22.4707, 70.0577),
            new CityLocation("Junagadh", 21.5222, 70.4579),
            new CityLocation("Gandhinagar", 23.2156, 72.6369),
            new CityLocation("Nadiad", 22.6939, 72.8616),
            new CityLocation("Morbi", 22.8173, 70.8377),
            new CityLocation("Gandhidham", 23.0833, 70.1333),
            new CityLocation("Bharuch", 21.7051, 72.9959)
        )),
        // Rajasthan - 12+ major cities
        new StateLocation("Rajasthan", Arrays.asList(
            new CityLocation("Jaipur", 26.9124, 75.7873),
            new CityLocation("Jodhpur", 26.2389, 73.0243),
            new CityLocation("Udaipur", 24.5854, 73.7125),
            new CityLocation("Kota", 25.2138, 75.8648),
            new CityLocation("Bikaner", 28.0229, 73.3119),
            new CityLocation("Ajmer", 26.4499, 74.6399),
            new CityLocation("Bharatpur", 27.2156, 77.4900),
            new CityLocation("Bhilwara", 25.3463, 74.6333),
            new CityLocation("Alwar", 27.5665, 76.6042),
            new CityLocation("Sikar", 27.6109, 75.1398),
            new CityLocation("Pali", 25.7718, 73.3233),
            new CityLocation("Sri Ganganagar", 29.9038, 73.8772)
        )),
        // Uttar Pradesh - 15+ major cities
        new StateLocation("Uttar Pradesh", Arrays.asList(
            new CityLocation("Lucknow", 26.8467, 80.9462),
            new CityLocation("Kanpur", 26.4499, 80.3319),
            new CityLocation("Agra", 27.1767, 78.0081),
            new CityLocation("Varanasi", 25.3176, 82.9739),
            new CityLocation("Meerut", 28.9845, 77.7064),
            new CityLocation("Allahabad", 25.4358, 81.8463),
            new CityLocation("Bareilly", 28.3670, 79.4304),
            new CityLocation("Gorakhpur", 26.7606, 83.3732),
            new CityLocation("Aligarh", 27.8974, 78.0880),
            new CityLocation("Moradabad", 28.8388, 78.7738),
            new CityLocation("Saharanpur", 29.9668, 77.5500),
            new CityLocation("Jhansi", 25.4484, 78.5685),
            new CityLocation("Firozabad", 27.1592, 78.3957),
            new CityLocation("Mathura", 27.4924, 77.6737),
            new CityLocation("Shahjahanpur", 27.8805, 79.9090)
        )),
        // Kerala - 10+ major cities
        new StateLocation("Kerala", Arrays.asList(
            new CityLocation("Kochi", 9.9312, 76.2673),
            new CityLocation("Thiruvananthapuram", 8.5241, 76.9366),
            new CityLocation("Kozhikode", 11.2588, 75.7804),
            new CityLocation("Thrissur", 10.5276, 76.2144),
            new CityLocation("Kollam", 8.8932, 76.6141),
            new CityLocation("Palakkad", 10.7867, 76.6548),
            new CityLocation("Malappuram", 11.0500, 76.0667),
            new CityLocation("Kannur", 11.8745, 75.3704),
            new CityLocation("Kasaragod", 12.4984, 74.9899),
            new CityLocation("Alappuzha", 9.4981, 76.3388)
        )),
        // Andhra Pradesh - 12+ major cities
        new StateLocation("Andhra Pradesh", Arrays.asList(
            new CityLocation("Visakhapatnam", 17.6868, 83.2185),
            new CityLocation("Vijayawada", 16.5062, 80.6480),
            new CityLocation("Guntur", 16.3067, 80.4365),
            new CityLocation("Nellore", 14.4426, 79.9865),
            new CityLocation("Kurnool", 15.8305, 78.0421),
            new CityLocation("Rajahmundry", 17.0005, 81.8040),
            new CityLocation("Tirupati", 13.6288, 79.4192),
            new CityLocation("Kadapa", 14.4753, 78.8298),
            new CityLocation("Anantapur", 14.6819, 77.6006),
            new CityLocation("Chittoor", 13.2149, 79.1001),
            new CityLocation("Ongole", 15.5036, 80.0444),
            new CityLocation("Eluru", 16.7050, 81.1043)
        )),
        // Madhya Pradesh - 12+ major cities
        new StateLocation("Madhya Pradesh", Arrays.asList(
            new CityLocation("Bhopal", 23.2599, 77.4126),
            new CityLocation("Indore", 22.7196, 75.8577),
            new CityLocation("Gwalior", 26.2183, 78.1828),
            new CityLocation("Jabalpur", 23.1815, 79.9864),
            new CityLocation("Ujjain", 23.1765, 75.7885),
            new CityLocation("Sagar", 23.8338, 78.7164),
            new CityLocation("Dewas", 22.9650, 76.0550),
            new CityLocation("Satna", 24.5854, 80.8292),
            new CityLocation("Ratlam", 23.3315, 75.0367),
            new CityLocation("Rewa", 24.5334, 81.2962),
            new CityLocation("Murwara", 23.8431, 80.3940),
            new CityLocation("Singrauli", 24.1997, 82.6753)
        )),
        // Bihar - 10+ major cities
        new StateLocation("Bihar", Arrays.asList(
            new CityLocation("Patna", 25.5941, 85.1376),
            new CityLocation("Gaya", 24.7914, 85.0002),
            new CityLocation("Bhagalpur", 25.2445, 86.9718),
            new CityLocation("Muzaffarpur", 26.1209, 85.3647),
            new CityLocation("Purnia", 25.7772, 87.4753),
            new CityLocation("Darbhanga", 26.1667, 85.9000),
            new CityLocation("Bihar Sharif", 25.1972, 85.5207),
            new CityLocation("Arrah", 25.5560, 84.6633),
            new CityLocation("Begusarai", 25.4180, 86.1309),
            new CityLocation("Katihar", 25.5405, 87.5854)
        )),
        // Odisha - 10+ major cities
        new StateLocation("Odisha", Arrays.asList(
            new CityLocation("Bhubaneswar", 20.2961, 85.8245),
            new CityLocation("Cuttack", 20.4625, 85.8820),
            new CityLocation("Rourkela", 22.2604, 84.8536),
            new CityLocation("Berhampur", 19.3142, 84.7941),
            new CityLocation("Sambalpur", 21.4702, 83.9701),
            new CityLocation("Puri", 19.8134, 85.8312),
            new CityLocation("Balasore", 21.4944, 86.9336),
            new CityLocation("Bhadrak", 21.0594, 86.4944),
            new CityLocation("Baripada", 21.9333, 86.7333),
            new CityLocation("Jharsuguda", 21.8556, 84.0061)
        )),
        // Punjab - 8+ major cities
        new StateLocation("Punjab", Arrays.asList(
            new CityLocation("Chandigarh", 30.7333, 76.7794),
            new CityLocation("Ludhiana", 30.9010, 75.8573),
            new CityLocation("Amritsar", 31.6340, 74.8723),
            new CityLocation("Jalandhar", 31.3260, 75.5762),
            new CityLocation("Patiala", 30.3398, 76.3869),
            new CityLocation("Bathinda", 30.2110, 74.9455),
            new CityLocation("Mohali", 30.7046, 76.7179),
            new CityLocation("Firozpur", 30.9251, 74.6108)
        )),
        // Haryana - 8+ major cities
        new StateLocation("Haryana", Arrays.asList(
            new CityLocation("Gurgaon", 28.4595, 77.0266),
            new CityLocation("Faridabad", 28.4089, 77.3178),
            new CityLocation("Panipat", 29.3909, 76.9635),
            new CityLocation("Ambala", 30.3753, 76.7821),
            new CityLocation("Yamunanagar", 30.1290, 77.2675),
            new CityLocation("Rohtak", 28.8955, 76.6066),
            new CityLocation("Hisar", 29.1492, 75.7216),
            new CityLocation("Karnal", 29.6857, 76.9905)
        )),
        // Jharkhand - 8+ major cities
        new StateLocation("Jharkhand", Arrays.asList(
            new CityLocation("Ranchi", 23.3441, 85.3096),
            new CityLocation("Jamshedpur", 22.8046, 86.2029),
            new CityLocation("Dhanbad", 23.7957, 86.4304),
            new CityLocation("Bokaro", 23.6693, 85.9783),
            new CityLocation("Deoghar", 24.4827, 86.7039),
            new CityLocation("Phusro", 23.7772, 85.9881),
            new CityLocation("Hazaribagh", 23.9924, 85.3612),
            new CityLocation("Giridih", 24.1876, 86.3088)
        )),
        // Assam - 8+ major cities
        new StateLocation("Assam", Arrays.asList(
            new CityLocation("Guwahati", 26.1445, 91.7362),
            new CityLocation("Silchar", 24.8167, 92.8000),
            new CityLocation("Dibrugarh", 27.4728, 94.9119),
            new CityLocation("Jorhat", 26.7509, 94.2037),
            new CityLocation("Nagaon", 26.3509, 92.6925),
            new CityLocation("Tinsukia", 27.4900, 95.3600),
            new CityLocation("Tezpur", 26.6331, 92.7916),
            new CityLocation("Barpeta", 26.3224, 91.0062)
        ))
    );
    
    // Comprehensive business types to fetch ALL available data
    private static final String[] BUSINESS_TYPES = {
        // Food & Dining
        "restaurant", "food", "cafe", "bakery", "fast_food", "meal_takeaway", "meal_delivery",
        "bar", "night_club", "liquor_store", "grocery_or_supermarket", "supermarket",
        
        // Shopping & Retail
        "store", "shopping_mall", "clothing_store", "shoe_store", "jewelry_store", 
        "electronics_store", "furniture_store", "home_goods_store", "hardware_store",
        "book_store", "toy_store", "department_store", "convenience_store",
        
        // Health & Medical
        "hospital", "pharmacy", "dentist", "doctor", "veterinary_care", "physiotherapist",
        "medical_lab", "health", "beauty_salon", "spa", "barber_shop", "hair_care",
        
        // Services
        "bank", "atm", "insurance_agency", "real_estate_agency", "travel_agency",
        "car_repair", "car_wash", "gas_station", "laundry", "dry_cleaner",
        "pet_store", "florist", "funeral_home", "cemetery",
        
        // Education & Government
        "school", "university", "library", "post_office", "local_government_office",
        "police", "fire_station", "courthouse", "embassy",
        
        // Entertainment & Recreation
        "gym", "park", "zoo", "aquarium", "museum", "art_gallery", "movie_theater",
        "bowling_alley", "amusement_park", "tourist_attraction",
        
        // Accommodation
        "lodging", "hotel", "motel", "campground", "rv_park",
        
        // Religious
        "church", "mosque", "hindu_temple", "synagogue", "place_of_worship",
        
        // Transportation
        "subway_station", "bus_station", "train_station", "airport", "taxi_stand",
        "bicycle_store", "motorcycle_dealer", "car_dealer",
        
        // Other
        "accounting", "lawyer", "roofing_contractor", "plumber", "electrician",
        "locksmith", "moving_company", "storage", "rv_rental"
    };
    
    @Override
    public void run(String... args) throws Exception {
        if (shouldSeedData) {
            seedBusinessData();
        }
    }
    
    public void seedBusinessData() {
        log.info("Starting data seeding process...");
        log.info("Budget mode: {}", budgetMode ? "ENABLED (Free tier friendly)" : "DISABLED (Full coverage)");
        
        // Check if data already exists
        long existingCount = businessRepository.count();
        if (existingCount > 0) {
            log.info("Database already contains {} businesses. Skipping seeding.", existingCount);
            return;
        }
        
        if (budgetMode) {
            seedBusinessDataBudget();
        } else {
            seedBusinessDataFull();
        }
    }
    
    public void seedBusinessDataBudget() {
        log.info("Starting BUDGET-FRIENDLY data seeding (stays within free tier)...");
        
        int totalBusinesses = 0;
        int apiCallCount = 0;
        int maxApiCalls = 10000; // Stay well within free tier
        
        for (StateLocation state : STATES) {
            if (apiCallCount >= maxApiCalls) {
                log.info("Reached API call limit ({}). Stopping seeding.", maxApiCalls);
                break;
            }
            
            log.info("Seeding businesses for state: {}", state.name);
            
            // Only process first 3 cities per state in budget mode
            List<CityLocation> citiesToProcess = state.cities.stream()
                .limit(3)
                .toList();
            
            for (CityLocation city : citiesToProcess) {
                if (apiCallCount >= maxApiCalls) {
                    log.info("Reached API call limit. Stopping seeding.");
                    break;
                }
                
                log.info("Seeding businesses for {} in {}", city.name, state.name);
                
                try {
                    List<Business> allBusinesses = new ArrayList<>();
                    
                    // Budget-friendly search strategy
                    String[][] businessTypeBatches = {
                        {"restaurant", "pharmacy", "store", "hospital", "bank"},
                        {"supermarket", "clothing_store", "electronics_store", "school", "hotel"}
                    };
                    
                    for (String[] batch : businessTypeBatches) {
                        if (apiCallCount >= maxApiCalls) break;
                        
                        try {
                            List<Business> batchBusinesses = googlePlacesService.searchNearbyBusinesses(
                                city.latitude, 
                                city.longitude, 
                                15000, // 15km radius
                                batch
                            );
                            allBusinesses.addAll(batchBusinesses);
                            apiCallCount++;
                            
                            // Delay between API calls
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                log.warn("Thread interrupted while sleeping");
                            }
                        } catch (Exception e) {
                            log.warn("Error fetching batch for {} in {}: {}", city.name, state.name, e.getMessage());
                        }
                    }
                    
                    // Filter and save
                    List<Business> uniqueBusinesses = filterUniqueBusinesses(allBusinesses);
                    List<Business> newBusinesses = filterAgainstExisting(uniqueBusinesses);
                    businessRepository.saveAll(newBusinesses);
                    
                    totalBusinesses += newBusinesses.size();
                    log.info("Added {} new businesses for {} in {} (API calls used: {}/{})", 
                        newBusinesses.size(), city.name, state.name, apiCallCount, maxApiCalls);
                    
                } catch (Exception e) {
                    log.error("Error seeding businesses for {} in {}: {}", city.name, state.name, e.getMessage());
                }
            }
        }
        
        log.info("Budget seeding completed. Total businesses added: {} (API calls used: {})", totalBusinesses, apiCallCount);
    }
    
    public void seedBusinessDataFull() {
        log.info("Starting FULL data seeding (comprehensive coverage)...");
        
        int totalBusinesses = 0;
        
        for (StateLocation state : STATES) {
            log.info("Seeding businesses for state: {}", state.name);
            
            for (CityLocation city : state.cities) {
                log.info("Seeding businesses for {} in {}", city.name, state.name);
                
                try {
                    List<Business> allBusinesses = new ArrayList<>();
                    
                    // Generate multiple search points around the city for comprehensive coverage
                    List<CityLocation> searchPoints = generateSearchPoints(city);
                    
                    for (CityLocation searchPoint : searchPoints) {
                        log.info("Searching around {} at {},{}", searchPoint.name, searchPoint.latitude, searchPoint.longitude);
                        
                        // Comprehensive search strategy with multiple approaches
                        String[][] businessTypeBatches = {
                            // Food & Dining
                            {"restaurant", "food", "cafe", "bakery", "fast_food", "meal_takeaway", "meal_delivery"},
                            {"bar", "night_club", "liquor_store", "grocery_or_supermarket", "supermarket"},
                            
                            // Shopping & Retail
                            {"store", "shopping_mall", "clothing_store", "shoe_store", "jewelry_store"},
                            {"electronics_store", "furniture_store", "home_goods_store", "hardware_store", "book_store"},
                            {"toy_store", "department_store", "convenience_store"},
                            
                            // Health & Medical
                            {"hospital", "pharmacy", "dentist", "doctor", "veterinary_care", "physiotherapist"},
                            {"medical_lab", "health", "beauty_salon", "spa", "barber_shop", "hair_care"},
                            
                            // Services & Government
                            {"bank", "atm", "insurance_agency", "real_estate_agency", "travel_agency"},
                            {"car_repair", "car_wash", "gas_station", "laundry", "dry_cleaner"},
                            {"pet_store", "florist", "funeral_home", "cemetery"},
                            {"school", "university", "library", "post_office", "local_government_office"},
                            {"police", "fire_station", "courthouse", "embassy"},
                            
                            // Entertainment & Recreation
                            {"gym", "park", "zoo", "aquarium", "museum", "art_gallery", "movie_theater"},
                            {"bowling_alley", "amusement_park", "tourist_attraction"},
                            
                            // Accommodation & Religious
                            {"lodging", "hotel", "motel", "campground", "rv_park"},
                            {"church", "mosque", "hindu_temple", "synagogue", "place_of_worship"},
                            
                            // Transportation & Other
                            {"subway_station", "bus_station", "train_station", "airport", "taxi_stand"},
                            {"bicycle_store", "motorcycle_dealer", "car_dealer"},
                            {"accounting", "lawyer", "roofing_contractor", "plumber", "electrician"},
                            {"locksmith", "moving_company", "storage", "rv_rental"}
                        };
                    
                        for (String[] batch : businessTypeBatches) {
                            try {
                                // Search with multiple radius strategies for comprehensive coverage
                                List<Business> batchBusinesses = new ArrayList<>();
                                
                                // Search with different radii to cover urban, suburban, and rural areas
                                int[] searchRadii = {5000, 10000, 20000, 30000}; // 5km, 10km, 20km, 30km
                                
                                for (int radius : searchRadii) {
                                    try {
                                        List<Business> radiusBusinesses = googlePlacesService.searchNearbyBusinesses(
                                            searchPoint.latitude, 
                                            searchPoint.longitude, 
                                            radius,
                                            batch
                                        );
                                        batchBusinesses.addAll(radiusBusinesses);
                                        
                                        // Small delay between radius searches
                                        try {
                                            Thread.sleep(200);
                                        } catch (InterruptedException ie) {
                                            Thread.currentThread().interrupt();
                                            log.warn("Thread interrupted while sleeping between radius searches");
                                        }
                                    } catch (Exception e) {
                                        log.warn("Error fetching radius {} for {} in {}: {}", radius, searchPoint.name, state.name, e.getMessage());
                                    }
                                }
                                allBusinesses.addAll(batchBusinesses);
                                
                                // Small delay between batches to respect API limits
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    log.warn("Thread interrupted while sleeping between batches");
                                }
                            } catch (Exception e) {
                                log.warn("Error fetching batch for {} in {}: {}", searchPoint.name, state.name, e.getMessage());
                            }
                        }
                    }
                    
                    // Filter out duplicates and check against existing data
                    List<Business> uniqueBusinesses = filterUniqueBusinesses(allBusinesses);
                    List<Business> newBusinesses = filterAgainstExisting(uniqueBusinesses);
                    businessRepository.saveAll(newBusinesses);
                    
                    totalBusinesses += newBusinesses.size();
                    log.info("Added {} new businesses for {} in {} (filtered {} duplicates)", 
                        newBusinesses.size(), city.name, state.name, 
                        uniqueBusinesses.size() - newBusinesses.size());
                    
                    // Add delay between cities to respect API limits
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Thread interrupted while sleeping between cities");
                    }
                    
                } catch (Exception e) {
                    log.error("Error seeding businesses for {} in {}: {}", city.name, state.name, e.getMessage());
                }
            }
            
            // Add longer delay between states
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("Thread interrupted while sleeping between states");
            }
        }
        
        log.info("Data seeding completed. Total businesses added: {}", totalBusinesses);
    }
    
    private List<Business> filterUniqueBusinesses(List<Business> businesses) {
        // Enhanced deduplication with multiple criteria
        return businesses.stream()
            .filter(business -> business.getBusinessName() != null && 
                              !business.getBusinessName().trim().isEmpty())
            .filter(business -> business.getLatitude() != null && 
                              business.getLongitude() != null)
            .filter(business -> business.getAddress() != null && 
                              !business.getAddress().trim().isEmpty())
            .collect(java.util.stream.Collectors.toMap(
                // Key: combination of name, coordinates, and address for uniqueness
                business -> createUniqueKey(business),
                business -> business,
                // If duplicate key found, keep the first one
                (existing, replacement) -> existing
            ))
            .values()
            .stream()
            .toList();
    }
    
    private String createUniqueKey(Business business) {
        // Create a unique key based on multiple criteria
        String name = business.getBusinessName().toLowerCase().trim();
        String address = business.getAddress().toLowerCase().trim();
        String coordinates = String.format("%.6f,%.6f", 
            business.getLatitude(), business.getLongitude());
        
        // Normalize business name (remove common variations)
        name = normalizeBusinessName(name);
        
        return name + "|" + address + "|" + coordinates;
    }
    
    private String normalizeBusinessName(String name) {
        // Remove common variations and normalize
        return name
            .replaceAll("\\s+", " ")  // Multiple spaces to single space
            .replaceAll("\\b(store|shop|market|mart|center|centre)\\b", "")  // Remove common suffixes
            .replaceAll("\\b(the|a|an)\\b", "")  // Remove articles
            .replaceAll("[^a-zA-Z0-9\\s]", "")  // Remove special characters
            .trim()
            .toLowerCase();
    }
    
    private List<Business> filterAgainstExisting(List<Business> newBusinesses) {
        // Get all existing businesses from database
        List<Business> existingBusinesses = businessRepository.findAll();
        
        // Create a set of existing business keys for fast lookup
        java.util.Set<String> existingKeys = existingBusinesses.stream()
            .map(this::createUniqueKey)
            .collect(java.util.stream.Collectors.toSet());
        
        // Filter out businesses that already exist
        return newBusinesses.stream()
            .filter(business -> !existingKeys.contains(createUniqueKey(business)))
            .toList();
    }
    
    private List<CityLocation> generateSearchPoints(CityLocation city) {
        List<CityLocation> searchPoints = new ArrayList<>();
        
        // Add the main city center
        searchPoints.add(new CityLocation(city.name + " Center", city.latitude, city.longitude));
        
        // Generate 8 points around the city in a circle (every 45 degrees)
        double radiusKm = 10; // 10km radius
        double earthRadius = 6371; // Earth's radius in km
        
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45); // 0, 45, 90, 135, 180, 225, 270, 315 degrees
            
            // Calculate new coordinates
            double lat1 = Math.toRadians(city.latitude);
            double lon1 = Math.toRadians(city.longitude);
            
            double lat2 = Math.asin(Math.sin(lat1) * Math.cos(radiusKm / earthRadius) +
                                  Math.cos(lat1) * Math.sin(radiusKm / earthRadius) * Math.cos(angle));
            
            double lon2 = lon1 + Math.atan2(Math.sin(angle) * Math.sin(radiusKm / earthRadius) * Math.cos(lat1),
                                          Math.cos(radiusKm / earthRadius) - Math.sin(lat1) * Math.sin(lat2));
            
            double newLat = Math.toDegrees(lat2);
            double newLon = Math.toDegrees(lon2);
            
            // Normalize longitude to [-180, 180]
            newLon = ((newLon + 180) % 360) - 180;
            
            searchPoints.add(new CityLocation(city.name + " Area " + (i + 1), newLat, newLon));
        }
        
        // Add additional points for comprehensive coverage
        // North, South, East, West points at 20km distance
        double[] directions = {0, 90, 180, 270}; // North, East, South, West
        String[] directionNames = {"North", "East", "South", "West"};
        
        for (int i = 0; i < directions.length; i++) {
            double angle = Math.toRadians(directions[i]);
            double lat1 = Math.toRadians(city.latitude);
            double lon1 = Math.toRadians(city.longitude);
            double distance = 20; // 20km
            
            double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / earthRadius) +
                                  Math.cos(lat1) * Math.sin(distance / earthRadius) * Math.cos(angle));
            
            double lon2 = lon1 + Math.atan2(Math.sin(angle) * Math.sin(distance / earthRadius) * Math.cos(lat1),
                                          Math.cos(distance / earthRadius) - Math.sin(lat1) * Math.sin(lat2));
            
            double newLat = Math.toDegrees(lat2);
            double newLon = Math.toDegrees(lon2);
            newLon = ((newLon + 180) % 360) - 180;
            
            searchPoints.add(new CityLocation(city.name + " " + directionNames[i], newLat, newLon));
        }
        
        return searchPoints;
    }
    
    public void clearAllData() {
        log.info("Clearing all business data...");
        businessRepository.deleteAll();
        log.info("All business data cleared.");
    }
    
    public long getBusinessCount() {
        return businessRepository.count();
    }
    
    public void seedSpecificCity(String cityName, double latitude, double longitude) {
        log.info("Seeding businesses for specific city: {} at {},{}", cityName, latitude, longitude);
        
        try {
            List<Business> allBusinesses = new ArrayList<>();
            
            // Search for businesses in batches with different business types
            String[][] businessTypeBatches = {
                {"restaurant", "pharmacy", "clothing_store", "hospital", "store"},
                {"supermarket", "grocery_store", "bakery", "electronics_store", "book_store"},
                {"jewelry_store", "furniture_store", "hardware_store", "beauty_salon", "barber_shop"},
                {"gas_station", "bank", "atm", "post_office", "school"},
                {"gym", "spa", "hotel", "travel_agency", "car_repair"},
                {"laundry", "pet_store", "florist", "toy_store", "shoe_store"}
            };
            
            for (String[] batch : businessTypeBatches) {
                try {
                    List<Business> batchBusinesses = googlePlacesService.searchNearbyBusinesses(
                        latitude, 
                        longitude, 
                        15000, // 15km radius for better coverage
                        batch
                    );
                    allBusinesses.addAll(batchBusinesses);
                    
                    // Small delay between batches to respect API limits
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Thread interrupted while sleeping between batches");
                    }
                } catch (Exception e) {
                    log.warn("Error fetching batch for {}: {}", cityName, e.getMessage());
                }
            }
            
            List<Business> uniqueBusinesses = filterUniqueBusinesses(allBusinesses);
            List<Business> newBusinesses = filterAgainstExisting(uniqueBusinesses);
            businessRepository.saveAll(newBusinesses);
            
            log.info("Added {} new businesses for {} (filtered {} duplicates)", 
                newBusinesses.size(), cityName, uniqueBusinesses.size() - newBusinesses.size());
            
        } catch (Exception e) {
            log.error("Error seeding businesses for {}: {}", cityName, e.getMessage());
        }
    }
    
    public void clearAndReseedData() {
        log.info("Clearing existing data and re-seeding...");
        clearAllData();
        seedBusinessData();
    }
    
    public void removeDuplicates() {
        log.info("Removing duplicate businesses from database...");
        
        List<Business> allBusinesses = businessRepository.findAll();
        log.info("Total businesses before deduplication: {}", allBusinesses.size());
        
        // Group businesses by unique key
        java.util.Map<String, List<Business>> groupedBusinesses = allBusinesses.stream()
            .collect(java.util.stream.Collectors.groupingBy(this::createUniqueKey));
        
        // Keep only the first business from each group
        List<Business> uniqueBusinesses = groupedBusinesses.values().stream()
            .map(businessList -> businessList.get(0))  // Keep first business from each group
            .toList();
        
        // Clear database and save unique businesses
        businessRepository.deleteAll();
        businessRepository.saveAll(uniqueBusinesses);
        
        int duplicatesRemoved = allBusinesses.size() - uniqueBusinesses.size();
        log.info("Removed {} duplicate businesses. Unique businesses: {}", 
            duplicatesRemoved, uniqueBusinesses.size());
    }
    
    private static class StateLocation {
        final String name;
        final List<CityLocation> cities;
        
        StateLocation(String name, List<CityLocation> cities) {
            this.name = name;
            this.cities = cities;
        }
    }
    
    private static class CityLocation {
        final String name;
        final double latitude;
        final double longitude;
        
        CityLocation(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
