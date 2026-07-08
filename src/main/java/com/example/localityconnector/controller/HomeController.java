package com.example.localityconnector.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the server-rendered Thymeleaf page shells. These are public; client-side scripts
 * redirect to login when no JWT is stored.
 *
 * <p>The legacy mutating JSON endpoints ({@code POST /saveLocation}, {@code POST /addListing})
 * have been removed. Item creation now goes exclusively through the canonical
 * {@code POST /api/items} ({@code ItemController}); location updates go through
 * {@code PUT /api/business/dashboard/location} ({@code BusinessDashboardController}). Both
 * return the standard {@code ApiResponse} envelope.</p>
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/admin")
    public String adminPanel() {
        return "admin-panel";
    }

    @GetMapping("/accuracy-test")
    public String accuracyTest() {
        return "coordinate-accuracy-test";
    }

    @GetMapping("/data-viewer")
    public String dataViewer() {
        return "business-data-viewer";
    }

    @GetMapping("/user")
    public String userPortal() {
        return "user_portal";
    }

    @GetMapping("/business")
    public String businessPortal() {
        return "business_portal";
    }

    @GetMapping("/user/login")
    public String userLogin() {
        return "user_login";
    }

    @GetMapping("/business/login")
    public String businessLogin() {
        return "business_login";
    }

    @GetMapping("/user/signup")
    public String userSignup() {
        return "user_signup";
    }

    @GetMapping("/business/signup")
    public String businessSignup() {
        return "business_signup";
    }

    @GetMapping("/user-home")
    public String userHome() {
        return "user_home";
    }

    @GetMapping("/enhanced-user-dashboard")
    public String enhancedUserDashboard() {
        return "enhanced_user_dashboard";
    }

    @GetMapping("/user-homepage")
    public String userHomepage() {
        return "user_homepage";
    }

    @GetMapping("/business-dashboard")
    public String businessDashboard() {
        return "redirect:/enhanced-business-dashboard";
    }

    @GetMapping("/enhanced-business-dashboard")
    public String enhancedBusinessDashboard() {
        return "enhanced_business_dashboard";
    }

    @GetMapping("/listing")
    public String listing() {
        return "listing";
    }

    @GetMapping("/addlisting")
    public String addListingPage() {
        return "addlisting";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "feedback";
    }

    @GetMapping("/update-business")
    public String updateBusiness() {
        // Stateless shell; the page loads the current profile via the JSON API using the stored JWT.
        return "update-business";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword() {
        return "reset-password";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }
}
