# Expense Tracker

> **AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System**

[![CI Pipeline](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203%20%7C%20Java%2021-007396.svg)]()
[![Frontend](https://img.shields.io/badge/frontend-React%20%7C%20Vite%20%7C%20Tailwind-61DAFB.svg)]()
[![AI Service](https://img.shields.io/badge/ai--service-FastAPI%20%7C%20Python%203.11+-3776AB.svg)]()
[![Database](https://img.shields.io/badge/database-PostgreSQL%2016%20%7C%20Flyway-336791.svg)]()
[![License](https://img.shields.io/badge/license-MIT-blue.svg)]()

---

## 1. Executive Summary

**Expense Tracker** is an enterprise-grade, full-stack personal finance engineering platform that bridges traditional double-entry financial accounting principles with modern artificial intelligence. It goes beyond static ledger keeping by providing intelligent transaction auto-categorization, deterministic and statistical expense forecasting, dynamic budget overrun prediction, multi-variate anomaly detection, and explainable, rule-grounded financial optimization recommendations.

---

## 2. Key Capabilities

- **Unified Financial Ledger:** Full lifecycle management of multi-currency income and expense entries with tagging, notes, receipt attachments, and recurring schedule support.
- **Hierarchical & Custom Categorization:** Built-in default taxonomy plus user-defined categories with distinct income/expense semantic separation.
- **Real-Time Financial Dashboard:** Instantaneous evaluation of cash flow, net balance, monthly burn rate, category-wise breakdown, and historical expenditure velocity.
- **Automated AI Categorization:** NLP-based pipeline using TF-IDF tokenization and regularized logistic classification (with future transformer expansion) returning calibrated confidence scores.
- **Predictive Expense Forecasting:** Time-series aggregation combined with machine learning (XGBoost baseline and sequential deep models) to project daily, weekly, and monthly spending with confidence intervals.
- **Budget Intelligence & Overrun Early-Warning:** Continuous tracking of budget burn down coupled with forecasting to issue timely alerts before thresholds are breached.
- **Unsupervised Anomaly Detection:** Isolation Forest-powered behavioral profiling identifying unusual transaction spikes and deviations without erroneously classifying them as fraud.
- **Explainable Financial Recommendations:** Quantitative rule engines analyzing savings rates, high-leakage expense segments, and actionable debt-to-savings optimizations.
- **Enterprise Security & Isolation:** Stateless JWT architecture, BCrypt password hashing, Spring Security 6 RBAC, strict tenant isolation at the SQL query level, and encrypted credentials.

---

## 3. Technology Stack

| Layer | Technologies | Key Libraries & Rationale |
| :--- | :--- | :--- |
| **Frontend** | React 18, Vite, JavaScript, Tailwind CSS | React Router 6, Axios, Recharts, Lucide Icons, Headless UI |
| **Backend** | Java 21 LTS, Spring Boot 3.3.x | Spring Security 6, Spring Data JPA, Hibernate, Bean Validation, JJWT |
| **Database** | PostgreSQL 16 | Flyway Database Migrations, UUID Primary Keys, NUMERIC Monetary Precision |
| **AI / ML Service** | Python 3.11+, FastAPI, Uvicorn | Pandas, NumPy, Scikit-learn, XGBoost, PyTorch, Joblib |
| **DevOps & Infrastructure** | Docker, Docker Compose, Git, AWS | Multi-stage Docker builds, PostgreSQL health checks, GitHub Actions CI/CD |

---

## 4. System Architecture

Expense Tracker employs a **modular monolithic backend** paired with a **dedicated high-performance Python AI microservice**, orchestrated behind a modern single-page frontend:

```
+-------------------------------------------------------------------------+
|                           Client Browser                                |
|             React 18 Single-Page Application (Vite + Tailwind)          |
+------------------------------------+------------------------------------+
                                     |
                                     | HTTPS / JSON REST API
                                     v
+-------------------------------------------------------------------------+
|                  Expense Tracker Backend (Spring Boot 3)                |
|  - Spring Security (JWT Filter)          - Transaction Management       |
|  - User Identity & Isolation Context     - Budget Engine & Alerts       |
|  - Financial Analytics & Reporting       - AI Orchestration Client      |
+--------------------+------------------------------------+---------------+
                     |                                    |
                     | JDBC (HikariCP)                    | Internal mTLS / HTTP REST
                     v                                    v
+------------------------------------+   +--------------------------------+
|       PostgreSQL 16 Database       |   |   FastAPI AI Service (Python)  |
|  - Flyway Versioned Migrations     |   |  - TF-IDF Categorizer          |
|  - Normalized Multi-Tenant Schema  |   |  - XGBoost Expense Forecaster  |
|  - Strict Foreign Keys & Indexes   |   |  - Isolation Forest Anomaly    |
+------------------------------------+   +--------------------------------+
```

---

## 5. Repository Structure

```
expense-tracker/
├── .editorconfig               # Cross-editor formatting specifications
├── .gitignore                  # Multi-stack git exclusions
├── README.md                   # Project overview and documentation index
├── docker/                     # Dockerfiles and docker-compose configurations
│   ├── docker-compose.yml      # Local orchestration (Postgres, Backend, AI, UI)
│   └── README.md
├── scripts/                    # Automation scripts (setup, seeding, linting)
│   └── README.md
├── frontend/                   # React + Vite + Tailwind CSS Application
│   └── README.md
├── backend/                    # Spring Boot 3 + Java 21 REST API
│   └── README.md
├── ai-service/                 # FastAPI + Scikit-Learn + XGBoost Machine Learning Service
│   └── README.md
└── docs/                       # Comprehensive Architecture & Engineering Documentation
    ├── requirements/           # SRS, functional, non-functional, and use cases
    │   ├── SRS.md
    │   ├── functional-requirements.md
    │   ├── non-functional-requirements.md
    │   └── use-cases.md
    ├── architecture/           # System design and Mermaid sequence diagrams
    │   ├── system-architecture.md
    │   └── system-architecture.mmd
    ├── database/               # Database schemas, ERD, and data dictionary
    │   ├── database-design.md
    │   ├── erd.mmd
    │   └── data-dictionary.md
    ├── api/                    # OpenAPI 3.0 REST specifications
    │   └── api-specification.md
    ├── frontend/               # UI component hierarchy and state design
    │   └── frontend-architecture.md
    ├── ai/                     # ML pipelines, datasets, and validation protocols
    │   ├── ml-architecture.md
    │   ├── dataset-strategy.md
    │   └── model-evaluation-plan.md
    └── development/            # Roadmap, Git conventions, and QA strategy
        ├── development-roadmap.md
        ├── git-workflow.md
        └── testing-strategy.md
```

---

## 6. Development Roadmap

- [x] **Phase 0:** Requirements, System Architecture, Database Schemas, API Specifications & Project Foundation.
- [ ] **Phase 1:** Spring Boot 3 Backend Initialization, PostgreSQL Setup, Flyway, JWT Authentication & Security.
- [ ] **Phase 2:** React Frontend Foundation, Layouts, Routing, State Management & Responsive Dashboard.
- [ ] **Phase 3:** Transaction Management, Categories, Filters, Pagination & Ledger Operations.
- [ ] **Phase 4:** AI Transaction Categorization (TF-IDF + Logistic Regression Pipeline).
- [ ] **Phase 5:** Time-Series Expense Forecasting (XGBoost & Rolling Aggregates).
- [ ] **Phase 6:** Budget Engine & Overrun Early-Warning Prediction.
- [ ] **Phase 7:** Unsupervised Anomaly Detection & Financial Optimization Recommendations.
- [ ] **Phase 8:** Full-Stack AI Integration & End-to-End Orchestration.
- [ ] **Phase 9:** Automated Testing, End-to-End Test Suite, Performance & Security Audit.
- [ ] **Phase 10:** Containerization, Docker Orchestration, AWS Cloud Infrastructure & Production Deployment.

---

## 7. Security & Engineering Standards

- **Zero Hardcoded Secrets:** All credentials, database passwords, and JWT signing keys are loaded strictly via environment variables or secret managers.
- **Monetary Precision:** Floats/doubles are strictly forbidden for currency operations; `NUMERIC(15,2)` in PostgreSQL and `BigDecimal` in Java guarantee zero rounding errors.
- **User Data Isolation:** Every query executing on personal financial records is filtered strictly against the authenticated user ID extracted from validated JWT claims.
- **Ethical AI Practices:** No synthetic or fabricated metrics; temporal train-test splits are mandatory to eliminate data leakage.

---

## 8. Git & Contribution Workflow

- **Official Remote:** `https://github.com/purush777196/expense-tracker.git`
- **Primary Branch:** `main`
- **Feature Branches:** `feature/<phase-or-feature-name>`
- **Commits:** Conventional Commits standard (`feat:`, `fix:`, `docs:`, `chore:`, `test:`, `refactor:`).

---

## 9. License

This project is licensed under the MIT License - see the LICENSE file for details.
