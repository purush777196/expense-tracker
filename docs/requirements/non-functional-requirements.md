# Non-Functional Requirements Specification (NFR)

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Performance Requirements (NFR-PERF)

| Req ID | Metric | Target Specification | Enforcement Mechanism |
| :--- | :--- | :--- | :--- |
| **NFR-PERF-01** | Transaction API Latency | P95 latency <= 150 ms, P99 <= 300 ms for transaction CRUD under 100 concurrent requests. | Spring Boot connection pooling (HikariCP) and composite SQL indexing. |
| **NFR-PERF-02** | AI Inference Latency | P95 inference latency <= 200 ms for batch categorization (<= 20 items) and <= 50 ms for single item. | Lightweight scikit-learn / XGBoost serialized pipelines loaded in worker memory. |
| **NFR-PERF-03** | Database Query Time | 95% of ledger read queries execute in under 25 ms. | Indexes on `(user_id, transaction_date DESC)` and foreign key lookups. |
| **NFR-PERF-04** | Client Initial Load | First Contentful Paint (FCP) <= 1.2s; Time to Interactive (TTI) <= 2.0s over standard 4G network. | Vite code splitting, dynamic imports, optimized Tailwind bundle. |
| **NFR-PERF-05** | Report Export Streaming | CSV export generation for up to 50,000 transactions streams in chunks with time-to-first-byte <= 500 ms. | Spring Web `StreamingResponseBody` writing directly to servlet output stream without buffer saturation. |

---

## 2. Security & Compliance (NFR-SEC)

| Req ID | Category | Specification |
| :--- | :--- | :--- |
| **NFR-SEC-01** | Password Storage | Passwords must be hashed using BCrypt with a minimum work factor of 12. Plaintext passwords must never be logged or retained in memory longer than necessary. |
| **NFR-SEC-02** | Authentication Tokens | Stateless JWT signed using HMAC-SHA256 (minimum 256-bit secret) or RSA-256. Access tokens expire in 15 minutes; refresh tokens expire in 7 days and are tracked in a revocable store. |
| **NFR-SEC-03** | Data Isolation | Every SQL query accessing user data must parameterize and explicitly filter by the authenticated `user_id`. Multi-tenant leaks are treated as severity-1 critical bugs. |
| **NFR-SEC-04** | Transport Security | All communications in transit must utilize TLS 1.3 (minimum TLS 1.2 with secure cipher suites). HTTP Strict Transport Security (HSTS) headers must be enforced. |
| **NFR-SEC-05** | Injection Prevention | 100% of database interactions must use JPA parameterized queries or Hibernate criteria builders. Direct string concatenation in SQL queries is strictly prohibited. |
| **NFR-SEC-06** | CORS & Headers | CORS configured strictly with explicit allowed origins, methods (`GET`, `POST`, `PUT`, `DELETE`), and headers. Set `Content-Security-Policy`, `X-Content-Type-Options: nosniff`, and `X-Frame-Options: DENY`. |
| **NFR-SEC-07** | Secret Management | Zero secrets in source control. All database credentials, JWT secrets, and API keys must be sourced through environment variables (`.env` excluded via `.gitignore`). |

---

## 3. Financial Integrity & Precision (NFR-FIN)

| Req ID | Category | Specification |
| :--- | :--- | :--- |
| **NFR-FIN-01** | Monetary Representation | Floating-point primitives (`float`, `double`) are banned for monetary calculations. All amounts must be stored as `NUMERIC(15,2)` in PostgreSQL and handled as `BigDecimal` in Java with explicit `RoundingMode.HALF_EVEN`. |
| **NFR-FIN-02** | ACID Compliance | Transaction creation, ledger balance updates, and budget recalculations must execute within atomic transactional boundaries (`@Transactional(isolation = Isolation.READ_COMMITTED)`). |
| **NFR-FIN-03** | Auditability | All records must maintain immutable audit fields: `created_at` (TIMESTAMPTZ), `updated_at` (TIMESTAMPTZ), and user attribution. |

---

## 4. Reliability, Availability & Fault Tolerance (NFR-REL)

| Req ID | Category | Specification |
| :--- | :--- | :--- |
| **NFR-REL-01** | Service Availability | System must achieve 99.9% uptime (excluding scheduled maintenance windows). |
| **NFR-REL-02** | Graceful AI Degradation | If the Python FastAPI AI service is unreachable, transaction operations must continue uninterrupted. The system logs a fallback event and queues the transaction for asynchronous categorization. |
| **NFR-REL-03** | Database Resilience | Connection pool health checks, automated reconnection policies, and transaction timeout ceilings (maximum 5000 ms). |
| **NFR-REL-04** | Error Handling | Standardized JSON error response format (RFC 7807 Problem Details) returning safe user messages without exposing stack traces. |

---

## 5. Scalability & Architectural Boundaries (NFR-SCAL)

| Req ID | Category | Specification |
| :--- | :--- | :--- |
| **NFR-SCAL-01** | Stateless Architecture | Spring Boot application nodes must be fully stateless, permitting horizontal scaling behind any Layer 7 load balancer (e.g., AWS ALB). |
| **NFR-SCAL-02** | Asynchronous ML Decoupling | Long-running model retraining or heavy batch aggregations must execute asynchronously without blocking HTTP request threads. |
| **NFR-SCAL-03** | Database Scalability | Schema prepared for future read-replica partitioning by separating analytical read-only reporting queries from transactional write operations. |

---

## 6. Usability & Accessibility (NFR-USE)

| Req ID | Category | Specification |
| :--- | :--- | :--- |
| **NFR-USE-01** | Accessibility | UI components comply with WCAG 2.1 Level AA standards, including color contrast ratios >= 4.5:1, keyboard navigability, and screen reader labels. |
| **NFR-USE-02** | Responsive Layouts | Clean user experience across viewport widths ranging from mobile (360px) to ultra-wide desktop (2560px) using Tailwind CSS responsive utilities. |
| **NFR-USE-03** | State Transparency | All asynchronous UI actions must provide instant visual feedback (skeleton loaders, spinners, or optimistic updates) and explicit error recovery prompts. |

---

## 7. Maintainability & Code Quality (NFR-MAINT)

| Req ID | Category | Specification |
| :--- | :--- | :--- |
| **NFR-MAINT-01** | Test Coverage | Minimum 80% automated unit and integration test coverage across backend service logic and ML feature transforms. |
| **NFR-MAINT-02** | Code Standards | Backend adheres to Java Google Style conventions; Python AI service conforms strictly to PEP 8 / Ruff guidelines; Frontend adheres to ESLint / Prettier rules. |
| **NFR-MAINT-03** | Database Versioning | 100% of schema definitions, migrations, and seed taxonomies are version-controlled strictly through sequential Flyway scripts (`V1__initial_schema.sql`, etc.). |
