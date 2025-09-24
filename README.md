# Locality Connector

Spring Boot + MongoDB web app for locality businesses and users.

## Prerequisites
- Java JDK 23 (installed and on PATH)
- MongoDB Community Server running on `localhost:27017`
- Maven Wrapper (included) – no global Maven required

## Quick Start
```powershell
# From project root
 # optional, avoids spaces-in-path issues on Windows
mvn spring-boot:run(make sure mvn bin file path is added successfully)
```
App runs at `http://localhost:8080`.

## Configuration
- `src/main/resources/application.properties`
  - Mongo DB: `spring.data.mongodb.database=localityconnector`
  - Server: `server.port=8080`

## Key Pages
- Dashboard: `GET /business-dashboard`
- Listing hub: `GET /listing`
- Add Listing: `GET /addlisting`
  - Single-purpose page to add an item for the logged-in business
  - Auto-fills business name from URL query, localStorage, or session

## Authentication (simplified)
- Business login: `POST /api/auth/business/login?email=...&password=...`
  - On success, session stores `loggedInBusinessName`
- Session helper: `GET /api/auth/session/business-name` → `{ businessName }`

## Items API
- Create item
  - `POST /api/items`
  - Body: `{ businessName, itemName, itemPrice, itemDescription }`
- List items for business
  - `GET /api/items?businessName=ACME`
- Update item
  - `PUT /api/items/{id}` with any of `{ itemName, itemPrice, itemDescription }`
- Delete item
  - `DELETE /api/items/{id}`

## Data Model (MongoDB)
- Collection: `businesses`
  - Fields: `businessName`, `ownerName`, `email`, `password`, `address`, `phoneNumber`, `category`, `latitude`, `longitude`, timestamps, etc.
- Collection: `items`
  - Fields: `businessId`, `businessName`, `name`, `price`, `description`, timestamps

## Troubleshooting
- Wrapper path error on Windows (spaces in user path):
  - Set a custom Maven user home: `setx MAVEN_USER_HOME D:\m2` then re-run
- 500 with “Local variable is required to be effectively final”
  - Stop the app, run a clean build: `.\mvnw.cmd -DskipTests clean package`, then rerun
- Business name is blank in UI
  - Ensure you have logged in as a business so the session is set
  - Or pass it explicitly: `/addlisting?businessName=Swaraj`

## Useful Commands
```powershell
# Build jar
.\mvnw.cmd -DskipTests package

# Run jar
java -jar .\target\localityconnector-0.0.1-SNAPSHOT.jar

# Verify MongoDB
mongosh --eval "use localityconnector; db.businesses.find().pretty()"
```

## Project Structure (high-level)
```
src/
  main/
    java/com/example/localityconnector/
      controller/  # MVC controllers & REST APIs
      repository/  # Spring Data repositories
      model/       # MongoDB document models
    resources/
      templates/   # Thymeleaf HTML pages
      application.properties
```
