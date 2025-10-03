# Database Schema - Locality Connector

## 🗄️ Database Overview

**Database Name**: `localityconnector`  
**Type**: MongoDB (NoSQL)  
**Port**: 27017  
**Host**: localhost

## 📊 Collections

### 1. Users Collection

**Collection Name**: `users`  
**Purpose**: Store user registration and profile information

#### Schema
```json
{
  "_id": "ObjectId",
  "name": "String (2-50 chars, required)",
  "email": "String (unique, validated, required)",
  "password": "String (min 6 chars, required)",
  "address": "String (required)",
  "phoneNumber": "String (optional)",
  "createdAt": "DateTime (auto-generated)",
  "updatedAt": "DateTime (auto-generated)",
  "isActive": "Boolean (default: true)"
}
```

#### Indexes
- `email` (unique, ascending)
- `_id` (primary key, auto-generated)

#### Sample Data
```json
{
  "_id": ObjectId("..."),
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "address": "123 Main St, New York, NY",
  "phoneNumber": "+1-555-0123",
  "createdAt": ISODate("2024-08-27T17:30:00Z"),
  "updatedAt": ISODate("2024-08-27T17:30:00Z"),
  "isActive": true
}
```

### 2. Businesses Collection

**Collection Name**: `businesses`  
**Purpose**: Store business registration and profile information

#### Schema
```json
{
  "_id": "ObjectId",
  "businessName": "String (2-100 chars, unique, required)",
  "ownerName": "String (2-50 chars, required)",
  "email": "String (unique, validated, required)",
  "password": "String (min 6 chars, required)",
  "address": "String (required)",
  "phoneNumber": "String (required)",
  "category": "String (required, enum)",
  "description": "String (optional)",
  "businessLicense": "String (optional)",
  "createdAt": "DateTime (auto-generated)",
  "updatedAt": "DateTime (auto-generated)",
  "isActive": "Boolean (default: true)",
  "isVerified": "Boolean (default: false)"
}
```

#### Business Categories
- `food` - Food and grocery stores
- `pharmacy` - Pharmacies and medical stores
- `clothing` - Clothing and fashion stores
- `stationary` - Stationary and office supplies
- `hospital` - Hospitals and medical facilities

#### Indexes
- `email` (unique, ascending)
- `businessName` (unique, ascending)
- `category` (ascending)
- `_id` (primary key, auto-generated)

#### Sample Data
```json
{
  "_id": ObjectId("..."),
  "businessName": "Fresh Foods Market",
  "ownerName": "Sarah Wilson",
  "email": "sarah@freshfoods.com",
  "password": "password123",
  "address": "100 Market St, New York, NY",
  "phoneNumber": "+1-555-0100",
  "category": "food",
  "description": "Fresh organic foods and groceries",
  "businessLicense": "LIC123456",
  "createdAt": ISODate("2024-08-27T17:30:00Z"),
  "updatedAt": ISODate("2024-08-27T17:30:00Z"),
  "isActive": true,
  "isVerified": true
}
```

## 🔍 Database Queries

### Find All Users
```javascript
db.users.find()
```

### Find User by Email
```javascript
db.users.findOne({ "email": "john@example.com" })
```

### Find All Businesses
```javascript
db.businesses.find()
```

### Find Businesses by Category
```javascript
db.businesses.find({ "category": "food" })
```

### Find Verified Businesses
```javascript
db.businesses.find({ "isVerified": true })
```

### Find Active Users
```javascript
db.users.find({ "isActive": true })
```

### Count Documents
```javascript
db.users.countDocuments()
db.businesses.countDocuments()
```

### Find Businesses by Location (Address contains)
```javascript
db.businesses.find({ "address": { $regex: "New York", $options: "i" } })
```

## 🛠️ Setup Commands

### Create Database
```javascript
use localityconnector
```

### Create Collections
```javascript
db.createCollection("users")
db.createCollection("businesses")
```

### Create Indexes
```javascript
// Users collection
db.users.createIndex({ "email": 1 }, { unique: true })

// Businesses collection
db.businesses.createIndex({ "email": 1 }, { unique: true })
db.businesses.createIndex({ "businessName": 1 }, { unique: true })
db.businesses.createIndex({ "category": 1 })
```

### Insert Sample Data
```javascript
// Insert sample user
db.users.insertOne({
    name: "Test User",
    email: "test@example.com",
    password: "password123",
    address: "Test Address",
    phoneNumber: "+1-555-0000",
    createdAt: new Date(),
    updatedAt: new Date(),
    isActive: true
})

// Insert sample business
db.businesses.insertOne({
    businessName: "Test Business",
    ownerName: "Test Owner",
    email: "business@example.com",
    password: "password123",
    address: "Test Business Address",
    phoneNumber: "+1-555-0001",
    category: "food",
    description: "Test business description",
    businessLicense: "TEST123",
    createdAt: new Date(),
    updatedAt: new Date(),
    isActive: true,
    isVerified: false
})
```

## 📈 Performance Optimization

### Indexes
- **Email indexes**: Fast user/business lookup by email
- **Business name index**: Fast business lookup by name
- **Category index**: Fast filtering by business category

### Query Optimization
- Use indexes for frequently queried fields
- Limit results using `.limit()` for large datasets
- Use projection to return only needed fields

### Example Optimized Queries
```javascript
// Return only name and email fields
db.users.find({}, { "name": 1, "email": 1, "_id": 0 })

// Limit results
db.businesses.find({ "category": "food" }).limit(10)

// Sort by creation date
db.businesses.find().sort({ "createdAt": -1 })
```

## 🔒 Security Considerations

### Data Validation
- Email format validation
- Password length requirements
- Required field validation
- Unique constraints on email and business names

### Access Control
- Database access should be restricted
- Use authentication for database connections
- Implement proper user roles and permissions

## 📊 Monitoring and Maintenance

### Database Statistics
```javascript
// Collection statistics
db.users.stats()
db.businesses.stats()

// Database statistics
db.stats()
```

### Backup and Restore
```bash
# Backup database
mongodump --db localityconnector --out backup/

# Restore database
mongorestore --db localityconnector backup/localityconnector/
```

### Health Checks
```javascript
// Check collection health
db.users.validate()
db.businesses.validate()

// Check index usage
db.users.getIndexes()
db.businesses.getIndexes()
```

## 🚀 Next Steps

1. **Data Migration**: Plan for future data migrations
2. **Backup Strategy**: Implement regular backup procedures
3. **Monitoring**: Set up database monitoring and alerting
4. **Scaling**: Plan for horizontal scaling if needed
5. **Security**: Implement additional security measures






















