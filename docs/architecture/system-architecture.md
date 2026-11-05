# System Architecture Specification

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Architectural Philosophy & Strategy

**Expense Tracker** adopts a **pragmatic modular monolithic architecture** for its primary transactional domain, combined with a **specialized, decoupled Python microservice** for artificial intelligence and machine learning inference.

### Key Architectural Tenets:
1. **Separation of Concerns:** Core business logic, ACID transactions, identity verification, and multi-tenant ledger management reside exclusively within the enterprise-grade Java 21 / Spring Boot 3 modular monolith.
2. **Dedicated ML Inference Boundary:** High-throughput numerical computation, tokenization, time-series transformations, and statistical inference are encapsulated within a Python 3.11+ / FastAPI runtime leveraging C-accelerated libraries (NumPy, Scikit-learn, XGBoost).
3. **Decoupled User Experience:** The presentation layer is a responsive, client-side rendered Single-Page Application (SPA) built using React 18, Vite, and Tailwind CSS, communicating strictly via documented RESTful APIs.
4. **Anti-Corruption & Fault Tolerance:** The backend accesses the AI service through resilient HTTP clients equipped with timeouts and graceful fallback mechanisms. If the AI service encounters downtime, standard ledger and budgeting operations continue without disruption.

---

## 2. High-Level System Architecture

```
+-----------------------------------------------------------------------------------+
|                                  CLIENT LAYER                                     |
|  +-----------------------------------------------------------------------------+  |
|  |             React 18 SPA (Vite, React Router, Tailwind CSS, Recharts)       |  |
|  +--------------------------------------+--------------------------------------+  |
+-----------------------------------------|-----------------------------------------+
                                          | HTTPS / JSON (Port 443 / 8080)
                                          v
+-----------------------------------------------------------------------------------+
|                             BACKEND APPLICATION LAYER                             |
|                           (Spring Boot 3.3.x / Java 21)                           |
|  +-----------------------------------------------------------------------------+  |
|  | Security & Filter Pipeline (JWT Authentication, CORS, Rate Limiting)         |  |
|  +-----------------------------------------------------------------------------+  |
|  | REST API Controllers (Auth, Transactions, Categories, Budgets, Analytics)   |  |
|  +-----------------------------------------------------------------------------+  |
|  | Service Domain Layer (Ledger Engine, Budget Calculator, Anomaly Handler)     |  |
|  +-----------------------------------------------------------------------------+  |
|  | AI Client Adapter (Resilient RestClient / WebClient with Fallback Logic)    |  |
|  +-----------------------------------------------------------------------------+  |
|  | Persistence Layer (Spring Data JPA, Hibernate ORM, Flyway Migrations)       |  |
|  +----------------------+-------------------------------------+-----------------+  |
+-------------------------|-------------------------------------|-------------------+
                          |                                     |
                          | JDBC (HikariCP)                     | Internal HTTP (Port 8000)
                          v                                     v
+-----------------------------------------+   +-------------------------------------+
|            PERSISTENCE LAYER            |   |          AI INFERENCE LAYER         |
|             (PostgreSQL 16)             |   |       (FastAPI / Python 3.11+)      |
|  - Users & Roles                        |   |  - TF-IDF Categorizer               |
|  - Categories & Transactions            |   |  - XGBoost Expense Forecaster       |
|  - Budgets & Goals                      |   |  - Isolation Forest Anomaly Engine  |
|  - AI Insights & Forecast Logs          |   |  - Spending Recommendation Engine   |
+-----------------------------------------+   +-------------------------------------+
```

---

## 3. Backend Modular Monolith Structure

The backend application is partitioned into clean, cohesive modules following domain-driven design principles:

```
backend/src/main/java/com/expensetracker/
├── config/                  # Global security, CORS, JPA auditing, Jackson configs
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── WebClientConfig.java
├── security/                # JWT filters, token provider, custom UserDetailsService
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   └── UserPrincipal.java
├── common/                  # Shared exceptions, standardized ApiResponse, pagination
│   ├── exception/
│   └── dto/
├── modules/
│   ├── auth/                # Registration, login, refresh, profile management
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   └── model/
│   ├── category/            # Default and custom categories
│   ├── transaction/         # Core financial ledger and recurring transactions
│   ├── budget/              # Monthly budgets and overrun alerts
│   ├── goal/                # Financial savings targets
│   ├── report/              # Aggregate reporting, cash flow, CSV export
│   └── ai/                  # AI orchestration, DTOs, and FastAPI integration client
│       ├── client/          # RestClient calling http://ai-service:8000
│       ├── service/         # Fallback logic and insight persistence
│       └── dto/             # Request/Response DTOs for ML inference
└── ExpenseTrackerApplication.java
```

---

## 4. AI Service Modular Architecture

The FastAPI service is organized for clarity, reusability, and continuous model updating:

```
ai-service/
├── app/
│   ├── main.py              # FastAPI application bootstrap and lifecycle handlers
│   ├── config.py            # Environment settings (Pydantic Settings)
│   ├── api/
│   │   ├── v1/
│   │   │   ├── router.py    # Master router
│   │   │   ├── endpoints/
│   │   │   │   ├── categorize.py     # TF-IDF category classification
│   │   │   │   ├── forecast.py       # XGBoost time-series projection
│   │   │   │   ├── overrun.py        # Budget overrun early warning
│   │   │   │   ├── anomaly.py        # Isolation Forest outlier scoring
│   │   │   │   └── recommend.py      # Rule-based spending recommendations
│   ├── models/              # Serialized pipeline artifacts (.pkl, .joblib)
│   ├── schemas/             # Pydantic request and response schemas
│   ├── services/            # Inference execution pipelines
│   │   ├── categorizer.py
│   │   ├── forecaster.py
│   │   ├── anomaly_detector.py
│   │   └── recommender.py
│   └── training/            # Offline training, cross-validation, and dataset scripts
│       ├── train_categorizer.py
│       ├── train_forecaster.py
│       └── evaluate_models.py
├── requirements.txt         # Pinned Python dependencies
└── Dockerfile
```

---

## 5. Inter-Service Communication & Failure Handling

### 5.1 Synchronous In-Flight Classification
1. The user inputs a transaction description in the React UI.
2. The UI debounces and queries `POST /api/v1/ai/categorize`.
3. Spring Boot receives the request, injects tenant context, and issues an HTTP POST to FastAPI `http://ai-service:8000/api/v1/categorize` with a 1500 ms connect/read timeout.
4. If FastAPI succeeds, Spring Boot returns the predicted category and confidence.
5. If FastAPI times out or returns HTTP 5xx, Spring Boot catches the exception, logs a warning, and returns a neutral fallback payload (`predictedCategory: null, confidence: 0.0, fallback: true`).

### 5.2 Asynchronous Anomaly Detection & Insights
1. When a transaction is committed to PostgreSQL, Spring Boot publishes an internal Spring Application Event (`TransactionCreatedEvent`).
2. An asynchronous event listener evaluates whether to query the Anomaly Detection endpoint.
3. If an anomaly is detected, an entry is inserted into the `ai_insights` table without impeding the user's synchronous write operation.

---

## 6. Security Architecture

- **Stateless Session Control:** No HTTP session state is stored on the server. Every authenticated request must carry a valid Bearer token in the `Authorization` header.
- **Strict Query Scoping:** Every Spring Data JPA repository method enforces `WHERE entity.user.id = :userId`. Direct repository lookups by entity ID alone without user verification are strictly forbidden.
- **Network Boundaries:** In Docker and AWS deployments, PostgreSQL and FastAPI ports are bind-mounted internally within private subnets/Docker networks; only the Spring Boot backend and Nginx reverse proxy expose public ports.
