# 🌐 Locality Connector

[![Spring Boot](https://img.shields.io/badge/Spring--Boot-3.3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-23-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![MongoDB](https://img.shields.io/badge/MongoDB--Atlas-Cloud-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/cloud/atlas)
[![Cloudflare](https://img.shields.io/badge/Cloudflare-DDoS%20Shield-F38020?style=for-the-badge&logo=cloudflare&logoColor=white)](https://www.cloudflare.com/)
[![AWS EC2](https://img.shields.io/badge/AWS-EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white)](https://aws.amazon.com/ec2/)
[![Cloudinary](https://img.shields.io/badge/Cloudinary-CDN-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white)](https://cloudinary.com/)

**Locality Connector** is a high-performance, full-stack local discovery and business ecosystem platform. It seamlessly bridges local residents with nearby storefronts, service providers, and neighborhood deals using real-time geohash proximity search, automated email/SMS OTP verification, Cloudinary CDN asset management, and stateless JWT security.

---

## 🌍 Live Production Deployment

* 🔗 **Live Website**: [https://locality-connector.in](https://locality-connector.in)
* 🔒 **SSL/HTTPS**: Let's Encrypt RSA 4096-bit Certificate
* 🛡️ **DDoS Protection**: Cloudflare Global Edge Proxy & WAF
* ☁️ **Host Infrastructure**: AWS EC2 (Ubuntu 26.04 LTS, Sydney `ap-southeast-2`)
* 🐳 **Containerization**: Multi-stage Docker Compose Architecture with Nginx Reverse Proxy

---

## 🛠️ Technology Stack

| Layer | Component / Tool | Details & Functionality |
| :--- | :--- | :--- |
| **Language & Core** | **Java 23** | Modern Java LTS runtime with Virtual Threads capability |
| **Framework** | **Spring Boot 3.3.x** | Spring MVC, Spring Security, Validation, Actuator, Cache |
| **Datastore** | **MongoDB Atlas Cloud** | Fully managed NoSQL Document Database with unique indexes & Geohashing |
| **Authentication** | **Stateless JWT (JJWT 0.11.5)** | HMAC-SHA256 tokens, JTI blacklisting, Secure HttpOnly cookies |
| **CDN & Image Hosting** | **Cloudinary Java SDK** | Cloud storage for Logos, Storefront covers, Item photos & auto WebP compression |
| **Email Service** | **Resend API** | Transactional emails & 4-Digit Registration OTP delivery |
| **SMS Service** | **Twilio REST API / Fast2SMS** | Mobile phone SMS OTP verification for multi-factor authentication |
| **Geolocation & Spatial** | **`ch.hsr:geohash` + Haversine** | Sub-kilometer geohash bounding box calculations & radius queries |
| **Reverse Proxy & Web** | **Nginx** | Port 443 SSL termination, HTTP/2 buffering, request routing |
| **DDoS & Web Security** | **Cloudflare WAF** | Edge proxy, rate limit enforcement, origin IP cloaking |
| **API Documentation** | **SpringDoc OpenAPI 3.0** | Interactive Swagger UI (`/swagger-ui.html`) |

---

## 🏗️ Architecture & Security Features

```text
[ Client (Browser / PWA) ]
          │
          ▼  (HTTPS Port 443)
┌──────────────────────────────────────┐
│  Cloudflare Global CDN & DDoS Shield │  <-- Origin IP Protection & WAF
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│       AWS EC2 Nginx Reverse Proxy    │  <-- SSL Termination & X-Forwarded Headers
└──────────────────┬───────────────────┘
                   │
                   ▼  (Port 8081)
┌──────────────────────────────────────┐
│   Spring Boot Application (Docker)   │
│ ┌──────────────────────────────────┐ │
│ │ RateLimitingFilter (60 req/min)  │ │  <-- Token Bucket Client-IP Throttling
│ ├──────────────────────────────────┤ │
│ │ JwtFilter & Spring Security      │ │  <-- Stateless JWT Validation & Role Control
│ └────────────────┬─────────────────┘ │
└──────────────────┼───────────────────┘
                   │
         ┌─────────┴─────────┬───────────────────┬──────────────────┐
         ▼                   ▼                   ▼                  ▼
┌────────────────┐  ┌─────────────────┐  ┌───────────────┐  ┌────────────────┐
│ MongoDB Atlas  │  │ Cloudinary CDN  │  │ Resend Email  │  │ Twilio SMS API │
│ (User/Business)│  │ (Logos/Photos)  │  │ (OTP Delivery)│  │ (Mobile OTPs)  │
└────────────────┘  └─────────────────┘  └───────────────┘  └────────────────┘
```

### 🔒 Core Security Highlights
1. **Stateless JWT Security**: Logins issue signed JWTs storing user identity and `ROLE_USER` / `ROLE_BUSINESS` / `ROLE_ADMIN` authorities. No server-side HTTP session state is created.
2. **Instant Logout & Revocation**: Calling `/api/auth/logout` blacklists the token's unique `jti` in MongoDB until expiration, purges `localStorage`, and expires browser cookies.
3. **Cross-Collection Email Uniqueness**: Dual-repository checking prevents an email address from being registered twice across both User and Business accounts.
4. **Brute-Force & Rate Limiting**: `RateLimitingFilter` enforces a token-bucket limit of 60 requests per minute per IP address, returning `HTTP 429 Too Many Requests`.
5. **Instant In-Memory Filtering**: User Dashboard filters businesses by category in **< 1 millisecond** using in-memory client state without network round-trip overhead.

---

## ⚙️ Environment Configuration (`.env`)

Create a `.env` file in the root directory before running the application:

```env
# --- Core Application ---
SERVER_PORT=8081
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET_KEY=change-me-to-a-long-random-secret-at-least-32-chars
ADMIN_EMAILS=swarajritik@gmail.com
CORS_ALLOWED_ORIGINS=https://locality-connector.in,https://www.locality-connector.in,http://localhost:8081

# --- MongoDB Atlas Connection ---
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.vhqx37e.mongodb.net/locality-connector

# --- Resend Email API ---
RESEND_API_KEY=re_xxxxxxxxxxxxxxxxxxxx
RESEND_FROM_EMAIL=no-reply@locality-connector.in

# --- Twilio SMS API ---
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_PHONE_NUMBER=+12345678901

# --- Cloudinary Image CDN ---
CLOUDINARY_CLOUD_NAME=wgot7nvl
CLOUDINARY_API_KEY=xxxxxxxxxxxxxxx
CLOUDINARY_API_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxx
```

---

## 🚀 Local Setup & Deployment Guide

### Prerequisites
* JDK 23+
* Maven 3.9+
* Docker & Docker Compose

### 1. Run Locally with Maven
```bash
# Clone the repository
git clone https://github.com/rswaraj09/Locality-Connector.git
cd Locality-Connector

# Copy environment template
cp .env.example .env

# Run locally
mvn spring-boot:run
```
The app will start at `http://localhost:8081`. Access Swagger UI at `http://localhost:8081/swagger-ui.html`.

### 2. Run with Docker Compose
```bash
docker compose up -d --build
```

### 3. Deploying to AWS EC2
```bash
# SSH into your EC2 instance
ssh -i "your-key.pem" ubuntu@your-ec2-ip

# Clone project and navigate
git clone https://github.com/rswaraj09/Locality-Connector.git
cd Locality-Connector

# Configure .env secrets
nano .env

# Launch application containers
docker compose up -d --build
```

---

## 📡 API Endpoint Reference

| Method & Endpoint | Auth / Role | Description |
| :--- | :--- | :--- |
| `POST /api/auth/user/signup` | Public | Register a new customer user account |
| `POST /api/auth/business/signup` | Public | Register a new business account |
| `POST /api/auth/user/login` | Public | Authenticate user & issue JWT token |
| `POST /api/auth/business/login` | Public | Authenticate business & issue JWT token |
| `POST /api/auth/otp/send` | Public | Send 4-digit OTP via Email (Resend) or SMS (Twilio) |
| `POST /api/auth/otp/verify` | Public | Verify 4-digit registration OTP code |
| `POST /api/auth/logout` | Any | Blacklist active JWT token & purge session cookies |
| `GET  /api/user/dashboard/businesses` | Public | List all active local businesses |
| `GET  /api/user/dashboard/businesses/nearby` | Public | Search nearby businesses using Geohash radius (lat, lng, radiusKm) |
| `POST /api/business/dashboard/logo` | BUSINESS | Upload business logo image to Cloudinary CDN |
| `POST /api/business/dashboard/storefront` | BUSINESS | Upload storefront cover image to Cloudinary CDN |
| `POST /api/items` | BUSINESS | Add catalog item/product with image upload |
| `POST /api/feedback` | USER | Submit 1–5 star rating and review for a business |
| `POST /api/favorites/toggle` | USER | Toggle favorite business bookmark |
| `GET  /health` | Public | System liveness, health check & database status |

---

## 📜 License

Distributed under the **MIT License**. See `LICENSE` for details.