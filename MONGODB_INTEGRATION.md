# MongoDB Integration for Locality Connector

This document explains the MongoDB integration for storing user and business signup data in separate collections.

## 🗄️ Database Structure

### Collections
- **`users`** - Stores user signup information
- **`businesses`** - Stores business signup information

### User Collection Schema
```json
{
  "_id": "ObjectId",
  "name": "String (2-50 chars)",
  "email": "String (unique, validated)",
  "password": "String (min 6 chars)",
  "address": "String",
  "phoneNumber": "String (optional)",
  "createdAt": "DateTime",
  "updatedAt": "DateTime",
  "isActive": "Boolean (default: true)"
}
```

### Business Collection Schema
```json
{
  "_id": "ObjectId",
  "businessName": "String (2-100 chars, unique)",
  "ownerName": "String (2-50 chars)",
  "email": "String (unique, validated)",
  "password": "String (min 6 chars)",
  "address": "String",
  "phoneNumber": "String",
  "category": "String (food, pharmacy, clothing, stationary, hospital)",
  "description": "String (optional)",
  "businessLicense": "String (optional)",
  "createdAt": "DateTime",
  "updatedAt": "DateTime",
  "isActive": "Boolean (default: true)",
  "isVerified": "Boolean (default: false)"
}
```

## 🔧 Configuration

### MongoDB Connection
- **Host**: localhost
- **Port**: 27017
- **Database**: localityconnector

### Application Properties
```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=localityconnector
```

## 🚀 API Endpoints

### User Signup
```http
POST /api/auth/user/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "address": "123 Main St, City",
  "phoneNumber": "+1234567890"
}
```

**Response:**
```json
{
  "message": "User registered successfully",
  "userId": "507f1f77bcf86cd799439011",
  "email": "john@example.com"
}
```

### Business Signup
```http
POST /api/auth/business/signup
Content-Type: application/json

{
  "businessName": "Fresh Foods Market",
  "ownerName": "Jane Smith",
  "email": "jane@freshfoods.com",
  "password": "password123",
  "address": "456 Market St, City",
  "phoneNumber": "+1234567890",
  "category": "food",
  "description": "Fresh organic foods and groceries",
  "businessLicense": "LIC123456"
}
```

**Response:**
```json
{
  "message": "Business registered successfully",
  "businessId": "507f1f77bcf86cd799439012",
  "businessName": "Fresh Foods Market",
  "email": "jane@freshfoods.com"
}
```

### User Login
```http
POST /api/auth/user/login?email=john@example.com&password=password123
```

**Response:**
```json
{
  "message": "Login successful",
  "userId": "507f1f77bcf86cd799439011",
  "name": "John Doe",
  "email": "john@example.com"
}
```

### Business Login
```http
POST /api/auth/business/login?email=jane@freshfoods.com&password=password123
```

**Response:**
```json
{
  "message": "Login successful",
  "businessId": "507f1f77bcf86cd799439012",
  "businessName": "Fresh Foods Market",
  "email": "jane@freshfoods.com",
  "category": "food"
}
```

### Get All Users (Admin)
```http
GET /api/auth/users
```

### Get All Businesses (Admin)
```http
GET /api/auth/businesses
```

## 🛠️ Setup Instructions

### 1. Install MongoDB
- Download and install MongoDB Community Server
- Start MongoDB service
- Create database: `localityconnector`

### 2. Start the Application
```powershell
.\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```

### 3. Test the APIs
Use tools like Postman, curl, or your browser to test the endpoints.

## 📝 Example Usage

### Using curl for User Signup
```bash
curl -X POST http://localhost:8080/api/auth/user/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "address": "123 Main St, City",
    "phoneNumber": "+1234567890"
  }'
```

### Using curl for Business Signup
```bash
curl -X POST http://localhost:8080/api/auth/business/signup \
  -H "Content-Type: application/json" \
  -d '{
    "businessName": "Fresh Foods Market",
    "ownerName": "Jane Smith",
    "email": "jane@freshfoods.com",
    "password": "password123",
    "address": "456 Market St, City",
    "phoneNumber": "+1234567890",
    "category": "food",
    "description": "Fresh organic foods and groceries",
    "businessLicense": "LIC123456"
  }'
```

## 🔒 Security Features

- **Email Validation**: Ensures valid email format
- **Password Requirements**: Minimum 6 characters
- **Unique Constraints**: Email and business names must be unique
- **Data Validation**: Input validation using Bean Validation
- **Error Handling**: Comprehensive error messages

## 📊 Business Categories

Supported business categories:
- `food` - Food and grocery stores
- `pharmacy` - Pharmacies and medical stores
- `clothing` - Clothing and fashion stores
- `stationary` - Stationary and office supplies
- `hospital` - Hospitals and medical facilities

## 🔍 Database Queries

### Find Users by Email
```java
Optional<User> user = userRepository.findByEmail("john@example.com");
```

### Find Businesses by Category
```java
List<Business> foodBusinesses = businessRepository.findByCategoryAndIsActiveTrue("food");
```

### Check if Email Exists
```java
boolean exists = userRepository.existsByEmail("john@example.com");
```

## 🚨 Error Handling

The API returns appropriate error messages for:
- Duplicate email addresses
- Duplicate business names
- Invalid email format
- Missing required fields
- Invalid login credentials

## 🔄 Next Steps

1. **Password Encryption**: Implement BCrypt password hashing
2. **JWT Authentication**: Add JWT token-based authentication
3. **Email Verification**: Implement email verification system
4. **Business Verification**: Add admin approval workflow
5. **Session Management**: Implement proper session handling



















