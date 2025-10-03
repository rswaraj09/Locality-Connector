// MongoDB Setup Script for Locality Connector
// Run this script in MongoDB shell or MongoDB Compass

// Switch to or create the localityconnector database
use localityconnector

// Create users collection with sample data
db.createCollection("users")

// Insert sample user data
db.users.insertMany([
    {
        name: "John Doe",
        email: "john@example.com",
        password: "password123",
        address: "123 Main St, New York, NY",
        phoneNumber: "+1-555-0123",
        createdAt: new Date(),
        updatedAt: new Date(),
        isActive: true
    },
    {
        name: "Jane Smith",
        email: "jane@example.com",
        password: "password123",
        address: "456 Oak Ave, Los Angeles, CA",
        phoneNumber: "+1-555-0456",
        createdAt: new Date(),
        updatedAt: new Date(),
        isActive: true
    },
    {
        name: "Mike Johnson",
        email: "mike@example.com",
        password: "password123",
        address: "789 Pine St, Chicago, IL",
        phoneNumber: "+1-555-0789",
        createdAt: new Date(),
        updatedAt: new Date(),
        isActive: true
    }
])

// Create businesses collection with sample data
db.createCollection("businesses")

// Insert sample business data
db.businesses.insertMany([
    {
        businessName: "Fresh Foods Market",
        ownerName: "Sarah Wilson",
        email: "sarah@freshfoods.com",
        password: "password123",
        address: "100 Market St, New York, NY",
        phoneNumber: "+1-555-0100",
        category: "food",
        description: "Fresh organic foods and groceries",
        businessLicense: "LIC123456",
        createdAt: new Date(),
        updatedAt: new Date(),
        isActive: true,
        isVerified: true
    },
    {
        businessName: "Health First Pharmacy",
        ownerName: "Dr. Robert Chen",
        email: "robert@healthfirst.com",
        password: "password123",
        address: "200 Health Ave, New York, NY",
        phoneNumber: "+1-555-0200",
        category: "pharmacy",
        description: "Complete pharmacy services and health products",
        businessLicense: "LIC789012",
        createdAt: new Date(),
        updatedAt: new Date(),
        isActive: true,
        isVerified: true
    },
    {
        businessName: "Style Boutique",
        ownerName: "Emma Davis",
        email: "emma@styleboutique.com",
        password: "password123",
        address: "300 Fashion Blvd, Los Angeles, CA",
        phoneNumber: "+1-555-0300",
        category: "clothing",
        description: "Trendy fashion and accessories",
        businessLicense: "LIC345678",
        createdAt: new Date(),
        updatedAt: new Date(),
        isActive: true,
        isVerified: true
    },
    {
        businessName: "Office Supplies Plus",
        ownerName: "David Brown",
        email: "david@officesupplies.com",
        password: "password123",
        address: "400 Business St, Chicago, IL",
        phoneNumber: "+1-555-0400",
        category: "stationary",
        description: "Complete office supplies and equipment",
        businessLicense: "LIC901234",
        createdAt: new Date(),
        updatedAt: new Date(),
        isActive: true,
        isVerified: true
    },
    {
        businessName: "City General Hospital",
        ownerName: "Dr. Lisa Anderson",
        email: "lisa@cityhospital.com",
        password: "password123",
        address: "500 Medical Center Dr, New York, NY",
        phoneNumber: "+1-555-0500",
        category: "hospital",
        description: "Full-service general hospital with emergency care",
        businessLicense: "LIC567890",
        createdAt: new Date(),
        updatedAt: new Date(),
        isActive: true,
        isVerified: true
    }
])

// Create indexes for better performance
db.users.createIndex({ "email": 1 }, { unique: true })
db.businesses.createIndex({ "email": 1 }, { unique: true })
db.businesses.createIndex({ "businessName": 1 }, { unique: true })
db.businesses.createIndex({ "category": 1 })

// Show the created collections
show collections

// Display sample data
print("\n=== Sample Users ===")
db.users.find().pretty()

print("\n=== Sample Businesses ===")
db.businesses.find().pretty()

print("\n=== Database Setup Complete ===")
print("Database: localityconnector")
print("Collections: users, businesses")
print("Indexes: email (unique), businessName (unique), category")






















