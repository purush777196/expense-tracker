# Quality Assurance & Testing Strategy

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Testing Philosophy & Test Pyramid

Quality assurance in **Expense Tracker** adheres to the classic Test Pyramid paradigm, emphasizing fast, isolated unit tests at the base, comprehensive component and API integration tests in the middle, and targeted end-to-end (E2E) workflow tests at the apex:

```
          / \
         /   \       E2E Workflow Tests (Playwright / Cypress)
        /-----\      - Complete User Journeys & Critical Paths
       /       \     Integration Tests (Spring MockMvc, Testcontainers, FastAPI TestClient)
      /---------\    - Database ACID, REST contracts, Security Filters, JWT Lifecycle
     /           \   Unit Tests (JUnit 5, Mockito, Vitest, Pytest)
    /-------------\  - Domain Business Logic, Mathematical Calculations, ML Transformers
```

---

## 2. Backend Testing Strategy (Java 21 / Spring Boot 3)

### 2.1 Frameworks & Tooling
- **Test Runner:** JUnit 5 (Jupiter)
- **Mocking & Assertion:** Mockito, AssertJ
- **Web Layer Testing:** Spring `MockMvc`
- **Database Integration:** Testcontainers with real PostgreSQL 16 image, verifying Flyway migrations and JPA queries.

### 2.2 Coverage Areas & Quality Gates
- **Service Layer:** 100% test coverage on monetary calculation logic, budget burn-rate math, and user-isolation guards.
- **Security & Authorization:** Explicit integration test suite verifying that unauthenticated requests receive HTTP 401, expired JWTs receive HTTP 401, and cross-user data access attempts receive HTTP 403 Forbidden.
- **Transactional Rollback:** Verify that failures during multi-step ledger updates cleanly roll back without corrupting database state.

---

## 3. Frontend Testing Strategy (React 18 / Vite)

### 3.1 Frameworks & Tooling
- **Test Runner:** Vitest (Vite-native test runner)
- **Component Testing:** React Testing Library (`@testing-library/react`)
- **API Mocking:** Mock Service Worker (`msw`) to intercept HTTP requests at the network layer.

### 3.2 Key Testing Suites
- **Component Rendering:** Verify that buttons, input fields, modals, and badges render accessible markup with correct ARIA attributes.
- **State & Context:** Test `AuthContext` to ensure login, logout, and token expiration transitions trigger correct UI redirects.
- **Visual Feedback:** Test that async loading states (skeleton loaders) display during network latency, and error states display on API failures.
- **Formatting Utilities:** Unit test currency and date formatters against various locales and edge cases (e.g. leap years, zero balances).

---

## 4. AI & Machine Learning Service Testing (Python / FastAPI)

### 4.1 Frameworks & Tooling
- **Test Runner:** Pytest
- **API Testing:** `fastapi.testclient.TestClient`
- **Numerical & Data Testing:** NumPy / Pandas testing utilities (`assert_frame_equal`, `assert_series_equal`).

### 4.2 Key Testing Suites
- **Preprocessing Pipeline Tests:** Validate that text normalizers strip store codes, dates, and noise tokens while retaining core merchant semantics.
- **Model Invariance & Sanity Tests:** Assert that input data shapes match expected feature dimensions, probability outputs sum to 1.0, and confidence values lie within $[0.0, 1.0]$.
- **Cold-Start Fallback Tests:** Verify that requesting forecasts for a user with $< 30$ historical transactions triggers the deterministic moving average fallback without throwing an unhandled exception.
- **API Endpoint Contract Tests:** Ensure that `/api/v1/categorize`, `/api/v1/forecast`, and `/api/v1/anomaly` return RFC-compliant JSON payloads adhering to Pydantic schemas.

---

## 5. Security & Static Analysis Quality Gates

| Gate / Tool | Target Standard | Failure Policy |
| :--- | :--- | :--- |
| **Backend Checkstyle** | Google Java Style | Build fails on style violations. |
| **Python Ruff / Flake8** | PEP 8 Compliance | CI fails on linting errors. |
| **Frontend ESLint** | React Recommended + Tailwind Clean | Build fails on unhandled linting warnings. |
| **Dependency Check** | OWASP Dependency-Check (CVE audit) | Build fails on High / Critical CVSS vulnerabilities. |
| **Code Coverage** | Minimum 80% line and branch coverage on domain services | PR merge blocked if coverage drops. |

---

## 6. End-to-End (E2E) Smoke Test Scenarios

The automated E2E test suite simulates the complete core lifecycle:
1. **User Registration:** Register new user `alice@example.com` with currency `USD`.
2. **Category Verification:** Verify default categories (Groceries, Housing, Dining, etc.) exist.
3. **Transaction Entry:** Log an expense: `$78.50` at `Whole Foods Market` with description `Weekly grocery shopping`.
4. **AI Auto-Categorization:** Verify the UI pre-selects `Groceries` with high confidence.
5. **Dashboard Reflection:** Verify KPI cards update: Expenses `$78.50`, Net Savings `-$78.50`.
6. **Budget Monitoring:** Set a `$500` budget for Groceries; verify burn rate meter displays `15.7%` spent.
7. **Report Export:** Request CSV export and assert file download with matching transaction details.
