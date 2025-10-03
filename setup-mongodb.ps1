# MongoDB Setup Script for Locality Connector
# Run this script in PowerShell as Administrator

Write-Host "=== MongoDB Setup for Locality Connector ===" -ForegroundColor Green

# Check if MongoDB is installed
Write-Host "Checking MongoDB installation..." -ForegroundColor Yellow

try {
    $mongoVersion = mongod --version 2>$null
    if ($mongoVersion) {
        Write-Host "✅ MongoDB is already installed" -ForegroundColor Green
        Write-Host $mongoVersion -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ MongoDB is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install MongoDB Community Server first:" -ForegroundColor Yellow
    Write-Host "1. Download from: https://www.mongodb.com/try/download/community" -ForegroundColor Cyan
    Write-Host "2. Install MongoDB" -ForegroundColor Cyan
    Write-Host "3. Add MongoDB to PATH" -ForegroundColor Cyan
    Write-Host "4. Run this script again" -ForegroundColor Cyan
    exit 1
}

# Start MongoDB service
Write-Host "Starting MongoDB service..." -ForegroundColor Yellow
try {
    Start-Service MongoDB
    Write-Host "✅ MongoDB service started" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Could not start MongoDB service. It might already be running." -ForegroundColor Yellow
}

# Wait a moment for MongoDB to start
Start-Sleep -Seconds 3

# Check if MongoDB is running
Write-Host "Checking MongoDB connection..." -ForegroundColor Yellow
try {
    $connection = Test-NetConnection -ComputerName localhost -Port 27017 -WarningAction SilentlyContinue
    if ($connection.TcpTestSucceeded) {
        Write-Host "✅ MongoDB is running on port 27017" -ForegroundColor Green
    } else {
        Write-Host "❌ MongoDB is not responding on port 27017" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Could not connect to MongoDB" -ForegroundColor Red
    exit 1
}

# Create and run the database setup script
Write-Host "Setting up database and collections..." -ForegroundColor Yellow

# Create a temporary JavaScript file for MongoDB shell
$tempScript = @"
use localityconnector

// Create users collection
db.createCollection("users")

// Create businesses collection  
db.createCollection("businesses")

// Create indexes
db.users.createIndex({ "email": 1 }, { unique: true })
db.businesses.createIndex({ "email": 1 }, { unique: true })
db.businesses.createIndex({ "businessName": 1 }, { unique: true })
db.businesses.createIndex({ "category": 1 })

print("Database setup complete!")
print("Database: localityconnector")
print("Collections: users, businesses")
"@

$tempScript | Out-File -FilePath "temp_setup.js" -Encoding UTF8

# Run the setup script
Write-Host "Running database setup script..." -ForegroundColor Yellow
try {
    mongo temp_setup.js
    Write-Host "✅ Database setup completed" -ForegroundColor Green
} catch {
    Write-Host "❌ Error running setup script" -ForegroundColor Red
    Write-Host "You can manually run the setup script:" -ForegroundColor Yellow
    Write-Host "1. Open MongoDB shell: mongo" -ForegroundColor Cyan
    Write-Host "2. Copy and paste the contents of mongodb_setup.js" -ForegroundColor Cyan
}

# Clean up temporary file
Remove-Item "temp_setup.js" -ErrorAction SilentlyContinue

Write-Host "`n=== Setup Complete ===" -ForegroundColor Green
Write-Host "Your MongoDB database is ready!" -ForegroundColor Green
Write-Host "Database: localityconnector" -ForegroundColor Cyan
Write-Host "Collections: users, businesses" -ForegroundColor Cyan
Write-Host "Port: 27017" -ForegroundColor Cyan

Write-Host "`nNext steps:" -ForegroundColor Yellow
Write-Host "1. Start your Spring Boot application" -ForegroundColor Cyan
Write-Host "2. Test the APIs using the endpoints in MONGODB_INTEGRATION.md" -ForegroundColor Cyan
Write-Host "3. Use the sample data for testing" -ForegroundColor Cyan

Write-Host "`nSample test data available:" -ForegroundColor Yellow
Write-Host "Users: john@example.com, jane@example.com, mike@example.com" -ForegroundColor Cyan
Write-Host "Password: password123" -ForegroundColor Cyan
Write-Host "Businesses: sarah@freshfoods.com, robert@healthfirst.com" -ForegroundColor Cyan






















