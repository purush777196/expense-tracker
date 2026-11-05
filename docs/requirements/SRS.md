# Software Requirements Specification (SRS)

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  
**Standards Compliance:** IEEE Std 830-1998  

---

## 1. Introduction

### 1.1 Purpose
This Software Requirements Specification (SRS) defines the complete software, functional, and non-functional requirements for **Expense Tracker** (AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System). It serves as the definitive engineering contract between systems architecture, backend engineering, frontend development, machine learning engineering, and quality assurance.

### 1.2 Scope
**Expense Tracker** is a secure, cloud-ready, full-stack personal finance engineering application. The system enables individual users to:
- Capture and reconcile multi-currency income and expense transactions.
- Automatically categorize unstructured transaction descriptions via an NLP-powered machine learning pipeline.
- Model and project future cash flows and burn rates through time-series forecasting.
- Establish multi-tier category and monthly budgets with predictive overrun alerting.
- Detect abnormal spending outliers using unsupervised anomaly detection models.
- Receive deterministic, explainable financial health recommendations.
- Aggregate financial telemetry into real-time interactive analytical dashboards and exportable audit reports.

### 1.3 Definitions, Acronyms, and Abbreviations
- **JWT:** JSON Web Token (RFC 7519)
- **RBAC:** Role-Based Access Control
- **CORS:** Cross-Origin Resource Sharing
- **UUID:** Universally Unique Identifier (RFC 4122 v4)
- **TF-IDF:** Term Frequency-Inverse Document Frequency
- **XGBoost:** Extreme Gradient Boosting
- **Isolation Forest:** Unsupervised anomaly detection algorithm based on decision trees
- **API:** Application Programming Interface
- **SPA:** Single-Page Application
- **ORM:** Object-Relational Mapping (Hibernate/JPA)
- **Flyway:** Open-source database migration and versioning tool

### 1.4 References
- IEEE Std 830-1998: IEEE Recommended Practice for Software Requirements Specifications
- RFC 6749: The OAuth 2.0 Authorization Framework
- RFC 7519: JSON Web Token (JWT)
- OWASP Top Ten 2021 Security Risks

### 1.5 Document Overview
Section 2 provides a high-level perspective of the system, user personas, operational environment, and constraints. Section 3 details external interfaces and specific operational requirements. Subsequent sections formalize security, auditability, and verification protocols.

---

## 2. Overall Description

### 2.1 Product Perspective
Expense Tracker is an autonomous three-tier distributed web application operating with a modular monolithic Java/Spring Boot backend, a specialized Python/FastAPI machine learning inference engine, and a React/Vite single-page frontend:
1. **Client Tier:** React 18 SPA delivered via modern web browsers over HTTPS.
2. **Application Tier:** Spring Boot 3 modular monolith running on Java 21 LTS providing business logic, orchestration, and security.
3. **Inference Tier:** Python 3.11+ FastAPI service exposing internal REST endpoints for ML model inference.
4. **Data Persistence Tier:** PostgreSQL 16 database utilizing Flyway migrations for schema management.

```
[ Web Browser / Client ] <---> [ Spring Boot Backend (Port 8080) ]
                                       |                 |
                                       v                 v
                        [ PostgreSQL DB (5432) ]   [ FastAPI AI Service (8000) ]
```

### 2.2 User Personas & Characteristics
- **Personal Budgeter:** Needs quick transaction entry, intuitive spending categorizations, visual budget status, and alerts before exceeding limits.
- **Financial Planner / Analyst User:** Seeks historical trends, forecasting over daily/weekly/monthly horizons, detailed category breakdowns, and exportable CSV reports.
- **Occasional / Novice Tracker:** Benefits from automated classification and prescriptive recommendations without requiring manual spreadsheet formulas.

### 2.3 General Constraints
- **Monetary Precision:** Under no circumstances shall floating-point data types (`float`, `double`) be used for currency representation. All amounts must be stored as `NUMERIC(15,2)` in PostgreSQL and handled as `java.math.BigDecimal` or integer cents.
- **Tenant Isolation:** All data access must strictly enforce `user_id` ownership constraints derived exclusively from verified JWT identity claims.
- **AI Reliability:** AI models must provide confidence scores. Cold-start thresholds must trigger deterministic fallback mechanisms rather than speculative or fabricated outputs.

### 2.4 Assumptions and Dependencies
- The application assumes client devices support modern ECMAScript standards (Chrome 90+, Safari 14+, Firefox 90+, Edge 90+).
- Secure network transport (TLS 1.3) is assumed in production environments.
- Reliable continuous timekeeping (NTP) is maintained across all host servers and database engines.

---

## 3. Specific System Requirements

### 3.1 Functional Requirements Summary
- **Module AUTH:** User Registration, Authentication, JWT Token lifecycle, Profile Management.
- **Module TXN:** Transaction Management (Income, Expense, CRUD, Filtering, Pagination, Sorting, Search).
- **Module CAT:** Category Management (Default system taxonomy, Custom user taxonomy, Type partitioning).
- **Module BUD:** Budget Management (Monthly limits, Category allocations, Real-time burn-rate tracking).
- **Module GOAL:** Financial Goals (Savings targets, Target deadlines, Deposit allocations, Progress status).
- **Module AI-CAT:** NLP Transaction Auto-Categorization (TF-IDF + Logistic Regression, confidence scoring).
- **Module AI-FCST:** Expense Forecasting (Time-series aggregations, XGBoost projections, interval bands).
- **Module AI-OVR:** Budget Overrun Warning Engine (Forecasted spend vs. remaining budget threshold).
- **Module AI-ANOM:** Unsupervised Anomaly Detection (Isolation Forest spending outlier flags).
- **Module AI-REC:** Financial Optimization Insights (Rule-based explainable recommendations).
- **Module RPT:** Financial Reporting & Data Export (Aggregations, Income vs Expense, CSV streaming).

### 3.2 External Interface Requirements
- **User Interface:** Responsive web interface built with React 18, Vite, and Tailwind CSS.
- **Hardware Interfaces:** Standard cloud compute instances (AWS EC2 / ECS / RDS) with x86_64 or ARM64 architectures.
- **Software Interfaces:**
  - RESTful HTTP/JSON communication between React and Spring Boot.
  - Internal RESTful HTTP/JSON communication between Spring Boot and FastAPI AI Service.
  - JDBC (HikariCP connection pool) communication between Spring Boot and PostgreSQL 16.
- **Communications Interfaces:** Standard TCP/IP, HTTPS/TLS 1.3, WebSockets/SSE (planned for push alerts).

---

## 4. System Non-Functional Attributes

### 4.1 Security
- Password hashing using BCrypt with minimum work factor of 12.
- Stateless authentication using HMAC-SHA256 (or RSA-256) signed JWTs.
- Automatic session invalidation and token expiration (access token: 15 minutes, refresh token: 7 days).
- Comprehensive CORS white-listing restricted to verified origin domains.
- SQL injection prevention via Spring Data JPA parameterized queries.
- XSS defense through automated output escaping and React Virtual DOM rendering.

### 4.2 Performance
- API response times for standard CRUD transactions under 150 ms (P95) under normal operational load.
- AI categorization inference latency under 200 ms (P95).
- Database read operations utilizing composite indexing on `(user_id, transaction_date)` and `(user_id, category_id)`.

### 4.3 Availability and Reliability
- Target availability: 99.9% uptime.
- Database backups executed automatically with point-in-time recovery capabilities.
- Fault-tolerant AI integration: In the event of FastAPI unavailability, transaction entry must succeed gracefully with a deferred categorization flag.

---

## 5. Verification & Acceptance Criteria
Every requirement specified herein must be validated through automated tests (unit, integration, regression) and formal acceptance suites prior to production staging. Detailed mapping is maintained in the Requirements Traceability Matrix.
