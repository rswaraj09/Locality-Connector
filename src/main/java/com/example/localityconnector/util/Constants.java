package com.example.localityconnector.util;

/**
 * Application-wide constants to avoid magic strings and improve maintainability.
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Firestore Collection Names
    public static final class Collections {
        public static final String BUSINESSES = "businesses";
        public static final String USERS = "users";
        public static final String ITEMS = "items";
        public static final String FEEDBACK = "feedback";
        public static final String TOKEN_BLACKLIST = "token_blacklist";
        public static final String LOGIN_ATTEMPTS = "login_attempts";
    }

    // JWT Claims
    public static final class JWT {
        public static final String ROLE_CLAIM = "role";
        public static final String USER_ROLE = "USER";
        public static final String BUSINESS_ROLE = "BUSINESS";
        public static final String ADMIN_ROLE = "ADMIN";
    }

    // Business Categories
    public static final class Categories {
        public static final String FOOD = "food";
        public static final String PHARMACY = "pharmacy";
        public static final String CLOTHING = "clothing";
        public static final String STATIONARY = "stationary";
        public static final String HOSPITAL = "hospital";
        public static final String GROCERY = "grocery";
    }

    // Error Messages
    public static final class ErrorMessages {
        public static final String BUSINESS_NOT_FOUND = "Business not found";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String ITEM_NOT_FOUND = "Item not found";
        public static final String INVALID_CREDENTIALS = "Invalid email or password";
        public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
        public static final String BUSINESS_NAME_ALREADY_EXISTS = "Business name already exists";
        public static final String UNAUTHORIZED = "Unauthorized access";
        public static final String INVALID_TOKEN = "Invalid or expired token";
    }

    // Success Messages
    public static final class SuccessMessages {
        public static final String LOGIN_SUCCESSFUL = "Login successful";
        public static final String SIGNUP_SUCCESSFUL = "Registration successful";
        public static final String UPDATE_SUCCESSFUL = "Update successful";
        public static final String DELETE_SUCCESSFUL = "Delete successful";
        public static final String ITEM_SAVED = "Item saved successfully";
    }

    // Validation Messages
    public static final class Validation {
        public static final String EMAIL_REQUIRED = "Email is required";
        public static final String PASSWORD_REQUIRED = "Password is required";
        public static final String NAME_REQUIRED = "Name is required";
        public static final String INVALID_EMAIL = "Invalid email format";
        public static final String PASSWORD_MIN_LENGTH = "Password must be at least 6 characters";
    }

    // Default Values
    public static final class Defaults {
        public static final int DEFAULT_RADIUS_KM = 5;
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final String DEFAULT_SORT_BY = "businessName";
        public static final String DEFAULT_SORT_DIRECTION = "asc";
    }

    // API Response Keys
    public static final class ResponseKeys {
        public static final String MESSAGE = "message";
        public static final String ERROR = "error";
        public static final String SUCCESS = "success";
        public static final String DATA = "data";
        public static final String TOKEN = "token";
        public static final String USER_ID = "userId";
        public static final String BUSINESS_ID = "businessId";
    }
}
