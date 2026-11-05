# Engineering Development Roadmap

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Roadmap Phase Progression Overview

```
[ Phase 0: Foundation & Specifications ] ---> COMPLETED
                    |
                    v
[ Phase 1: Spring Boot Backend & Security ]
                    |
                    v
[ Phase 2: React Frontend & Dashboard Shell ]
                    |
                    v
[ Phase 3: Transaction & Category Ledger ]
                    |
                    v
[ Phase 4: AI Transaction Categorization ]
                    |
                    v
[ Phase 5: Time-Series Expense Forecasting ]
                    |
                    v
[ Phase 6: Budget Management & Overrun Engine ]
                    |
                    v
[ Phase 7: Anomaly Detection & Financial Recommendations ]
                    |
                    v
[ Phase 8: Full-Stack AI Integration & Orchestration ]
                    |
                    v
[ Phase 9: Testing, Auditing & Performance Hardening ]
                    |
                    v
[ Phase 10: Dockerization, AWS Cloud & Production Deployment ]
```

---

## 2. Granular Phase Breakdown

### Phase 0: Requirements, Architecture, Database Design & Project Setup
- **Status:** **Completed**
- **Core Deliverables:**
  - Complete software requirements specifications (SRS, functional, non-functional, use cases).
  - System architecture documents and Mermaid sequence/structural diagrams.
  - Relational database schema definitions, ERD, and data dictionary.
  - REST API OpenAPI specifications across all application domains.
  - Frontend component architecture and design specifications.
  - AI/ML pipeline architecture, dataset strategy, and model evaluation protocols.
  - Multi-stack `.gitignore`, `.editorconfig`, root documentation, and GitHub setup.
- **Verification Criteria:** All architectural documents created, validated, and pushed to `main`.

---

### Phase 1: Spring Boot 3 Backend Initialization & Security
- **Target Branch:** `feature/backend-foundation`
- **Core Deliverables:**
  - Maven project setup (Spring Boot 3.3.x, Java 21, Spring Security, Spring Data JPA, Flyway).
  - PostgreSQL container connectivity and HikariCP connection pool configuration.
  - Flyway migration scripts `V1__initial_schema.sql` and `V2__seed_system_categories.sql`.
  - JPA Entity models (`User`, `Category`, `Transaction`, `Budget`, etc.) and repositories.
  - Security configuration: BCrypt password encoder, JWT authentication filter, UserDetailsService.
  - Auth REST API: Registration, login, token refresh, and user profile endpoints.
- **Verification Criteria:** Integration tests validating user registration, JWT generation, secure route protection, and Flyway schema execution pass with 100% success.

---

### Phase 2: React Frontend Foundation & Dashboard UI
- **Target Branch:** `feature/dashboard`
- **Core Deliverables:**
  - React 18 + Vite scaffolding with Tailwind CSS and Lucide icons.
  - React Router v6 setup with public and protected route layouts.
  - Axios API client with automatic JWT bearer attachment and 401 refresh interceptors.
  - AuthContext provider handling login state, tokens, and user session persistence.
  - Responsive Dashboard Layout: Sidebar, Header, KPI Summary Cards, and Chart Placeholders.
- **Verification Criteria:** Seamless login/logout flows, responsive UI navigation across desktop and mobile viewports, zero console errors.

---

### Phase 3: Transaction & Category Management
- **Target Branch:** `feature/transaction-management`
- **Core Deliverables:**
  - Spring Boot Transaction & Category service logic with pagination and filtering specifications.
  - Category CRUD APIs and Transaction CRUD APIs with Bean Validation.
  - React Transaction Management view: Paginated ledger table, date/category filters, full-text search.
  - Transaction Entry Modal with real-time field validation.
- **Verification Criteria:** Unit and integration tests for transaction ledger operations, ACID isolation, and accurate mathematical aggregations.

---

### Phase 4: AI Transaction Categorization
- **Target Branch:** `feature/ai-categorization`
- **Core Deliverables:**
  - Python 3.11+ FastAPI service scaffolding with Uvicorn.
  - Dataset ingestion script and text preprocessing pipeline.
  - TF-IDF + Logistic Regression training script and serialized model artifact.
  - FastAPI `/api/v1/categorize` endpoint with confidence scoring.
  - Spring Boot AI client adapter and React UI auto-suggestion badge integration.
- **Verification Criteria:** Offline test set Macro F1 score verified; API inference latency $< 50$ ms; UI correctly auto-fills high-confidence categories.

---

### Phase 5: Time-Series Expense Forecasting
- **Target Branch:** `feature/expense-forecasting`
- **Core Deliverables:**
  - Time-series feature engineering pipeline (lags, rolling means, cyclical calendar indicators).
  - XGBoost regression model training and validation protocol.
  - FastAPI `/api/v1/forecast` endpoint serving 7, 14, and 30-day projected spends.
  - React Recharts spending forecast visualization with historical spend vs. projected trend.
  - Cold-start fallback engine for accounts with limited historical transactions.
- **Verification Criteria:** Chronological validation test executed; cold-start accounts display deterministic fallback with clear user messaging.

---

### Phase 6: Budget Management & Overrun Prediction
- **Target Branch:** `feature/budget-management`
- **Core Deliverables:**
  - Monthly budget limit and category-specific budget envelope services in Spring Boot.
  - Budget Overrun Prediction integration comparing real-time spend velocity with AI forecast.
  - React Budget Dashboard: Visual progress meters, remaining balance indicators, and overrun warning badges.
- **Verification Criteria:** Accurate burn rate calculation; overrun alerts trigger correctly when spending pace threatens budget exhaustion.

---

### Phase 7: Anomaly Detection & Financial Recommendations
- **Target Branch:** `feature/financial-recommendations`
- **Core Deliverables:**
  - Isolation Forest unsupervised outlier model identifying atypical spending.
  - Explainable financial recommendation engine implementing 50/30/20 heuristics and subscription audits.
  - React Recommendations & Insights Center with actionable adjustment prompts and dismissal handling.
- **Verification Criteria:** Anomalous transactions flagged without punitive fraud classifications; recommendation engine yields explainable outputs.

---

### Phase 8: Full-Stack AI Integration & Orchestration
- **Target Branch:** `feature/ai-integration`
- **Core Deliverables:**
  - End-to-end integration across React, Spring Boot, PostgreSQL, and FastAPI.
  - Asynchronous event bus for non-blocking ML insight generation.
  - Resilience4j circuit breakers and timeout handling on Spring Boot AI RestClient.
- **Verification Criteria:** Full simulated user journey operates seamlessly under normal and degraded AI service states.

---

### Phase 9: Quality Assurance, Security Audit & Optimization
- **Target Branch:** `feature/qa-and-audit`
- **Core Deliverables:**
  - End-to-end integration and API regression test suites.
  - OWASP Top 10 security audit (SQL injection, XSS, broken auth, CORS validation).
  - Database indexing review and query execution plan optimization (`EXPLAIN ANALYZE`).
- **Verification Criteria:** Zero critical vulnerabilities, test coverage $\ge 80\%$, P95 response times under target thresholds.

---

### Phase 10: Dockerization, AWS Cloud & Deployment
- **Target Branch:** `feature/devops-deployment`
- **Core Deliverables:**
  - Multi-stage Dockerfiles for Backend, Frontend (Nginx), and AI Service.
  - Unified `docker-compose.yml` for local production-like orchestration.
  - AWS cloud deployment manifests (ECS/EC2, RDS PostgreSQL, S3, CloudFront).
  - GitHub Actions CI/CD automated pipeline for build, test, and container packaging.
- **Verification Criteria:** One-command local stack initialization; production container health checks pass cleanly.
