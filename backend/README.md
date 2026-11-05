# Expense Tracker - Backend Service

> **Core Enterprise REST API & Financial Ledger Engine**

[![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.3.4%20%7C%20Java%2021-007396.svg)]()
[![Security](https://img.shields.io/badge/security-Spring%20Security%206%20%7C%20JJWT-green.svg)]()
[![Database](https://img.shields.io/badge/database-PostgreSQL%2016%20%7C%20Flyway-336791.svg)]()
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

---

## 1. Overview

The **Expense Tracker Backend** is a high-performance modular monolith built on **Java 21 LTS** and **Spring Boot 3.3.4**. It handles user authentication, stateless JWT session lifecycles, ACID financial ledger records, categorical budget management, Flyway-versioned relational schema evolutions, and integration with the Python AI inference service.

---

## 2. Technology Stack & Key Libraries

- **Language:** Java 21 LTS
- **Framework:** Spring Boot 3.3.4
- **Security:** Spring Security 6, BCrypt (work factor 12), JJWT 0.12.6
- **Persistence:** Spring Data JPA, Hibernate ORM 6.5.x
- **Database:** PostgreSQL 16 (H2 in PostgreSQL mode for isolated unit/integration tests)
- **Database Migrations:** Flyway 10.x
- **Validation:** Jakarta Bean Validation (Hibernate Validator)
- **API Documentation:** SpringDoc OpenAPI 2.6.0 (Swagger UI)
- **Connection Pool:** HikariCP

---

## 3. Package Structure

```
backend/src/main/java/com/expensetracker/
├── ExpenseTrackerApplication.java       # Spring Boot main bootstrap class
├── config/
│   ├── CorsConfig.java                  # CORS origin and header configurations
│   └── OpenApiConfig.java               # Swagger 3.0 / OpenAPI metadata & bearer scheme
├── security/
│   ├── SecurityConfig.java              # Filter chain, stateless session, public routes
│   ├── JwtAuthenticationFilter.java     # Per-request JWT Bearer token validator
│   ├── JwtService.java                  # HMAC-SHA256 token generation & claims parser
│   ├── CustomUserDetailsService.java    # Loads user by email for Spring Security
│   ├── UserPrincipal.java               # UserDetails implementation with UUID
│   ├── JwtAuthenticationEntryPoint.java # 401 Unauthorized JSON error responder
│   └── CustomAccessDeniedHandler.java   # 403 Forbidden JSON error responder
├── common/
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java  # RFC 7807 problem details error controller
│   │   ├── DuplicateResourceException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── InvalidTokenException.java
│   │   └── BadRequestException.java
│   └── response/
│       ├── ApiResponse.java             # Standardized success envelope
│       ├── ErrorResponse.java           # RFC 7807 problem detail envelope
│       └── ValidationError.java         # Field-specific rejection metadata
├── auth/
│   ├── controller/AuthController.java   # /register, /login, /refresh, /me
│   ├── service/AuthService.java         # Identity verification & token generation
│   └── dto/
│       ├── RegisterRequest.java
│       ├── LoginRequest.java
│       ├── RefreshTokenRequest.java
│       └── AuthResponse.java
├── user/
│   ├── entity/User.java                 # JPA Entity mapped to users table
│   ├── entity/Role.java                 # ROLE_USER, ROLE_ADMIN
│   ├── repository/UserRepository.java   # Spring Data repository
│   ├── service/UserService.java         # Profile retrieval and password updates
│   ├── controller/UserController.java   # /api/v1/users/me endpoints
│   └── dto/
│       ├── UserProfileResponse.java     # Safe user projection (no password hash)
│       ├── UpdateProfileRequest.java
│       └── ChangePasswordRequest.java
└── category/
    ├── entity/Category.java             # JPA Entity mapped to categories table
    ├── entity/CategoryType.java         # INCOME, EXPENSE
    └── repository/CategoryRepository.java
```

---

## 4. Database Migrations (Flyway)

Database migrations are located in `backend/src/main/resources/db/migration/`:
- **`V1__initial_schema.sql`**:
  - Creates `users` table with UUID primary key (`DEFAULT gen_random_uuid()`), unique normalized email constraint, BCrypt password hash column, currency format check, and audit timestamps.
  - Creates `categories` table with UUID primary key, foreign key referencing `users(id)`, type check (`INCOME`, `EXPENSE`), and unique constraint per user and type.
  - Creates database indexes on `users(email)` and `categories(user_id, type)`.
- **`V2__seed_system_categories.sql`**:
  - Seeds 9 default expense categories (Housing & Rent, Groceries, Utilities, Transportation, Dining & Food, Healthcare, Entertainment, Shopping & Retail, Other Expense).
  - Seeds 4 default income categories (Salary, Freelance & Contracts, Investments & Dividends, Other Income).

---

## 5. Configuration Profiles & Environment Variables

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Web server listening port |
| `SPRING_PROFILES_ACTIVE`| `dev` | Active Spring profile (`dev`, `test`, `prod`) |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `expense_tracker` | Database name |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | *(Must be set in prod)* | 256-bit HMAC-SHA256 secret key |
| `JWT_EXPIRATION_MS` | `900000` | Access token lifespan (15 minutes) |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` | Refresh token lifespan (7 days) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed frontend origins |

---

## 6. Implemented API Endpoints (Phase 1)

### Public Endpoints:
- `POST /api/v1/auth/register` - Create account, hash password via BCrypt, return JWT tokens.
- `POST /api/v1/auth/login` - Authenticate email and password, return JWT tokens.
- `POST /api/v1/auth/refresh` - Exchange active refresh token for a newly minted access token.
- `GET /actuator/health` - Health check status.
- `GET /swagger-ui.html` - Interactive Swagger API documentation.

### Authenticated Endpoints (`Authorization: Bearer <token>`):
- `GET /api/v1/auth/me` - Retrieve currently authenticated user's profile.
- `GET /api/v1/users/me` - Retrieve authenticated user's profile.
- `PUT /api/v1/users/me` - Update full name or default currency.
- `PUT /api/v1/users/me/password` - Change password with current password verification.

---

## 7. Build and Testing Instructions

### Run Automated Tests:
```bash
# Execute unit and integration tests (32 tests across Auth, User, JWT, Flyway, and Security)
mvn clean test
```

### Package Application:
```bash
# Build executable production fat JAR
mvn clean package -DskipTests
```

### Run Locally:
```bash
# Start backend on port 8080
mvn spring-boot:run
```
