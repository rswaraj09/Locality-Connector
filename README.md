# Locality Connector

A Spring Boot backend that connects local customers with nearby businesses. It provides
stateless JWT authentication, a business catalog, proximity search (geohash + Haversine),
customer feedback/ratings, and an admin moderation surface, backed by Google Cloud
Firestore.

---

## Tech stack

| Concern        | Choice                                          |
| -------------- | ----------------------------------------------- |
| Language       | Java 23                                         |
| Framework      | Spring Boot 3.3.x (Web, Security, Validation)   |
| Auth           | Stateless JWT (jjwt 0.11.5)                      |
| Datastore      | Google Cloud Firestore                          |
| Geo            | `ch.hsr:geohash` + Haversine distance           |
| API docs       | springdoc-openapi (Swagger UI)                  |
| Build / test   | Maven, JUnit 5, Mockito, JaCoCo (60% gate)      |
| Packaging      | Docker (multi-stage), Docker Compose, GitLab CI |

---

## Architecture overview

```
client (Thymeleaf pages + JS)
        |  Authorization: Bearer <JWT>
        v
  Spring Security (stateless)
        |  JwtFilter -> sets principal = entityId, authorities = ROLE_*
        v
  Controllers  ->  Services  ->  Firestore repositories  ->  Firestore
```

- **Stateless auth**: login issues a JWT carrying `sub` (email), `sub_id` (entity id),
  `sub_name` (display name), `roles`, and a unique `jti`. No HTTP session is created.
- **Logout** revokes the token by storing its `jti` in a Firestore blacklist until it
  expires; a scheduled job purges expired entries.
- **Brute-force protection**: 5 failed logins within 15 minutes locks an account for 30
  minutes; locked logins return `429 Too Many Requests` with a `Retry-After` header.
- **Uniform responses**: every REST endpoint returns `ApiResponse<T>`
  (`{ success, data, error, timestamp }`); errors are normalized by a
  `@RestControllerAdvice` global handler.

---

## Getting started

### Prerequisites

- JDK 23
- Maven 3.9+
- A Firebase project + service account JSON
- (Optional) Google Maps API keys for geocoding / nearby search

### Configuration

Configuration is environment-driven. Copy the template and fill in values:

```bash
cp .env.example .env
```

| Variable                         | Purpose                                              | Required |
| -------------------------------- | ---------------------------------------------------- | -------- |
| `JWT_SECRET_KEY`                 | HMAC-SHA256 signing key (>= 32 chars)                | Yes      |
| `ADMIN_EMAILS`                   | Comma-separated emails granted the ADMIN role        | No       |
| `CORS_ALLOWED_ORIGINS`           | Comma-separated allowed origins                      | No       |
| `FIREBASE_SERVICE_ACCOUNT_PATH`  | `classpath:` resource or file path to the SA JSON    | Yes      |
| `SERVER_PORT`                    | HTTP port (default `8081`)                           | No       |
| `GOOGLE_MAPS_API_KEY`            | Google Places key                                    | No       |
| `GOOGLE_MAPS_GEOCODING_API_KEY`  | Google Geocoding key                                 | No       |

The app validates that `JWT_SECRET_KEY` is at least 32 bytes and fails fast on startup
otherwise.

### Firebase Credentials Setup (CRITICAL)

The application cannot start without valid Firebase service account credentials.

1. Go to your Firebase Console -> Project Settings -> Service Accounts.
2. Click **Generate new private key** and download the JSON file.
3. Rename the downloaded file to `serviceAccountKey.json`.
4. Place it in `src/main/resources/serviceAccountKey.json` for local development (or inside a `config/` directory if running via Docker Compose).
5. Set the environment variable `FIREBASE_SERVICE_ACCOUNT_PATH=classpath:serviceAccountKey.json` (or `file:./config/serviceAccountKey.json`).

### Run locally

```bash
# Export the variables from your .env, then:
mvn spring-boot:run
```

The app starts on `http://localhost:8081`. Swagger UI is at
`http://localhost:8081/swagger-ui.html` and the OpenAPI spec at `/v3/api-docs`.

### Run the tests + coverage

```bash
mvn verify
```

This runs the unit/integration tests and enforces the JaCoCo line-coverage gate (60%).
The HTML report is written to `target/site/jacoco/index.html`.

---

## Running with Docker

```bash
cp .env.example .env          # fill in secrets
mkdir -p config               # place serviceAccountKey.json here
# Ensure .env has FIREBASE_SERVICE_ACCOUNT_PATH=file:/app/config/serviceAccountKey.json
docker compose up --build
```

The `docker-compose.yml` mounts `./config:/app/config:ro`, ensuring the container can read your `serviceAccountKey.json` securely without baking secrets into the Docker image.

The image is multi-stage (Maven build -> JRE runtime), runs as a non-root user, and
exposes a container `HEALTHCHECK` that calls `/health` (which in turn verifies Firestore
connectivity and returns `503` when the datastore is unreachable).

---

## Authentication flow (client)

1. `POST /api/auth/{user|business}/login` with `{ email, password }`.
2. Store the returned `data.token` (the bundled `js/auth.js` keeps it in `localStorage`).
3. Send it on protected calls as `Authorization: Bearer <token>` (use `LCAuth.authFetch`).
4. `POST /api/auth/logout` revokes the token server-side.

---

## Key endpoints

| Method & path                                | Role      | Description                          |
| -------------------------------------------- | --------- | ----------------------------------- |
| `POST /api/auth/user/signup`                 | public    | Register a user                     |
| `POST /api/auth/business/signup`             | public    | Register a business                 |
| `POST /api/auth/user/login`                  | public    | User login -> JWT                   |
| `POST /api/auth/business/login`              | public    | Business login -> JWT               |
| `POST /api/auth/refresh`                     | any       | Refresh current valid JWT           |
| `POST /api/auth/logout`                      | any       | Revoke current token                |
| `GET  /api/items/business/{id}`              | public    | List a business's catalog           |
| `POST /api/items`                            | BUSINESS  | Create a catalog item               |
| `PUT/DELETE /api/items/{id}`                 | BUSINESS  | Update / delete an owned item       |
| `GET  /api/business/dashboard/feedback`      | BUSINESS  | Feedback for the logged-in business |
| `GET  /api/business/dashboard/customers`     | BUSINESS  | Distinct reviewers (own data only)  |
| `PUT  /api/business/dashboard/profile`       | BUSINESS  | Update the business profile         |
| `POST /api/feedback`                         | USER      | Submit a 1-5 star rating            |
| `GET  /api/feedback/business/{id}`           | public    | List feedback / average rating      |
| `GET  /api/feedback/business/{id}/histogram` | public    | Rating distribution histogram       |
| `POST /api/feedback/{id}/report`             | any       | Report feedback as inappropriate    |
| `POST /api/favorites/toggle`                 | USER      | Toggle a business favorite          |
| `GET  /api/admin/**`                         | ADMIN     | Moderation & management             |
| `GET  /health`                               | public    | Liveness + Firebase connectivity    |

Server-rendered Thymeleaf pages (e.g. `/user`, `/business`, dashboards) are public shells;
the client scripts guard access using the stored JWT and redirect to login when needed.

---

## Project structure

```
src/main/java/com/example/localityconnector/
  config/        OpenAPI, Firebase, RestTemplate beans
  controller/    REST + page controllers
  dto/           Request/response payloads (validated)
  exception/     Custom exceptions + global handler
  model/         Firestore-backed domain models
  repository/    Firestore repositories
  security/      JwtUtil, JwtFilter, SecurityConfig
  service/       Business logic
  util/          ApiResponse, SecurityUtils, GeolocationUtils
src/main/resources/
  static/css|js  Frontend assets (main.css, auth.js, validation.js)
  templates/     Thymeleaf pages + fragments/layout.html
  firestore.indexes.json  Composite index definitions
```

---

## CI/CD

`.gitlab-ci.yml` defines three stages: **build** (compile), **test** (`mvn verify` with
JaCoCo gate + JUnit reports), and **package** (build the runnable jar on `main`/tags).

---

## License

MIT.

---

## Firestore Security Rules

This application talks to Firestore **exclusively through the Firebase Admin SDK**.
The Admin SDK runs with service-account privileges and **bypasses Firestore Security
Rules entirely**, so a `firestore.rules` file would give a false sense of protection
for application traffic. The previously bundled `firestore.rules` has therefore been
**removed**. Authorization is enforced in the application layer by Spring Security
(stateless JWT, `ROLE_*` authorities) plus per-resource ownership checks in the
controllers.

If you later add a client that talks to Firestore directly with the Firebase client
SDK, reintroduce a `firestore.rules` file at that point and deploy it with
`firebase deploy --only firestore:rules`.

---

## Required configuration (fail-fast at startup)

The following keys are validated when the application context is built; a blank or
missing value throws `IllegalStateException` at startup instead of silently degrading:

| Property | Used by | Behavior when blank |
| -------- | ------- | ------------------- |
| `JWT_SECRET_KEY` (`jwt.secret`) | `JwtUtil` | Startup fails (also rejected if < 32 bytes) |
| `google.maps.api.key` / geocoding key | `GooglePlacesService` | Startup fails: "Maps API key must be configured" |
| `mappls.api.key` | `DirectionsService` (Mappls) | Startup fails: "Maps API key must be configured" |

Provide these via environment variables (see `.env.example`) before booting the app.
#   L o c a l i t y - C o n n e c t o r  
 