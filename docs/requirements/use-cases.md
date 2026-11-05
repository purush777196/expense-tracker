# System Use Cases Specification

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. System Actors

- **Guest User:** Unauthenticated individual visiting the platform to register or log in.
- **Authenticated User:** Primary account holder managing transactions, budgets, goals, and reviewing AI insights.
- **Spring Boot Backend System:** Core server orchestrating authentication, business workflows, persistence, and external service calls.
- **FastAPI AI Service:** Machine learning microservice executing NLP categorization, time-series forecasting, and anomaly scoring.
- **PostgreSQL Database Engine:** Persistent ACID-compliant relational data store.

---

## 2. Core Use Cases

### UC-01: User Registration & Initial Profile Configuration
- **Actor:** Guest User
- **Preconditions:** Guest is connected to the web application over HTTPS.
- **Main Success Scenario:**
  1. User navigates to `/register` and submits name, email, password, and preferred currency.
  2. Frontend performs client-side validation on field completeness and password complexity.
  3. Frontend sends `POST /api/v1/auth/register` payload to Spring Boot.
  4. Backend verifies email uniqueness, hashes password with BCrypt (factor 12), and persists new User entity.
  5. Backend provisions default system categories (Groceries, Housing, Dining, Salary, etc.) for the user.
  6. Backend generates JWT access and refresh tokens and returns them in the response.
  7. Frontend stores tokens securely and redirects user to `/dashboard`.
- **Alternative / Exception Flows:**
  - *4a. Email already registered:* Backend returns HTTP 409 Conflict. Frontend displays "An account with this email already exists."
  - *2a. Password fails complexity:* Frontend blocks submission and displays validation guidelines.
- **Postconditions:** User account exists in database; default categories are initialized; active session established.

---

### UC-02: User Authentication & Session Maintenance
- **Actor:** Guest User / Authenticated User
- **Preconditions:** Registered user account exists.
- **Main Success Scenario:**
  1. User enters email and password at `/login`.
  2. Backend validates credentials against stored BCrypt hash.
  3. Backend returns short-lived JWT access token (15 mins) and refresh token (7 days).
  4. React application stores access token in memory and refresh token in secure HTTP-only cookie or encrypted storage.
  5. Subsequent HTTP requests include `Authorization: Bearer <token>`.
  6. Upon token expiration, Axios interceptor catches 401, issues `POST /api/v1/auth/refresh`, and transparently replays original request.
- **Alternative / Exception Flows:**
  - *2a. Invalid credentials:* Backend returns HTTP 401 Unauthorized with generic message.
  - *6a. Refresh token expired/revoked:* User redirected to `/login` with session expiry notification.
- **Postconditions:** Authenticated context established.

---

### UC-03: Log Transaction with AI Auto-Categorization
- **Actor:** Authenticated User
- **Preconditions:** User is logged in.
- **Main Success Scenario:**
  1. User clicks "Add Transaction" modal on Dashboard or Transactions page.
  2. User enters amount, type (`EXPENSE`), merchant name (e.g., "Trader Joe's"), and description ("Weekly organic groceries").
  3. As the user completes the description, frontend debounces a request to `POST /api/v1/ai/categorize` with description and merchant.
  4. Spring Boot forwards payload to FastAPI AI Service (`/predict/category`).
  5. FastAPI TF-IDF + Logistic Regression model predicts category "Groceries" with confidence `0.94`.
  6. Frontend receives prediction and pre-selects "Groceries" with a visual badge: "AI Suggested (94% confidence)".
  7. User confirms or overrides category selection, specifies date, and submits form.
  8. Spring Boot validates payload, attaches `user_id`, saves to PostgreSQL, and triggers real-time budget recalculation.
  9. Frontend updates transaction table and dashboard metrics optimistically.
- **Alternative / Exception Flows:**
  - *5a. Model confidence is low (< 0.60):* Category dropdown remains empty or suggests top 3 candidates with user confirmation prompt.
  - *4a. AI service unreachable:* Backend catches timeout, allows manual category selection without error, and logs warning.
- **Postconditions:** Transaction committed to database; category totals and budget metrics refreshed.

---

### UC-04: Filter, Search, and Audit Transaction History
- **Actor:** Authenticated User
- **Preconditions:** Transactions exist in user ledger.
- **Main Success Scenario:**
  1. User navigates to `/transactions`.
  2. User applies multi-facet filters: Date range (Last 30 Days), Category ("Dining"), Type ("EXPENSE"), Search text ("Cafe").
  3. Frontend sends `GET /api/v1/transactions?page=0&size=20&category=...&query=Cafe`.
  4. Spring Boot constructs dynamic JPA Specification filtering by `user_id` and selected criteria.
  5. Paginated results returned with total count and page metadata.
  6. User views filtered transactions in sortable table and can trigger edit/delete on individual items.
- **Postconditions:** Filtered transaction subset rendered without mutating underlying database records.

---

### UC-05: Create Monthly & Category Budgets with Overrun Monitoring
- **Actor:** Authenticated User
- **Preconditions:** User has active expense categories.
- **Main Success Scenario:**
  1. User visits `/budgets` and selects "Set Budget" for the current month.
  2. User assigns overall monthly spend limit ($3,000) and specific category caps (Dining: $400, Groceries: $600).
  3. Spring Boot validates no negative limits and persists `Budget` entities with unique constraint on `(user_id, category_id, year, month)`.
  4. System computes current actual spend vs limit and requests overrun projection from AI service.
  5. AI Service projects month-end spending based on current daily burn rate and historical trend.
  6. If projected spend exceeds budget limit, the UI highlights the category in amber/red with warning: "Projected to exceed budget by $85 by day 28".
- **Postconditions:** Active budgets established and monitored against real-time and projected expenses.

---

### UC-06: Inspect Spending Dashboard & Forecasting Projections
- **Actor:** Authenticated User
- **Preconditions:** User has recorded transactions over at least several days.
- **Main Success Scenario:**
  1. User opens `/dashboard`.
  2. Frontend issues parallel requests for: KPI summaries, monthly spending curve, category donut distribution, and AI forecast.
  3. Spring Boot serves aggregate metrics and requests 30-day forecast from FastAPI AI Service.
  4. AI service computes daily rolling features and runs XGBoost time-series regression.
  5. Dashboard renders interactive Recharts multi-line graph: Solid line for actual historical spend, dashed line with shaded confidence interval for projected spend.
- **Alternative / Exception Flows:**
  - *4a. Cold-start user (< 30 transactions):* System displays historical moving average and a polite banner: "Forecast accuracy will improve after 30 logged transactions."
- **Postconditions:** Financial position and forward-looking projections clearly visualized.

---

### UC-07: Review Anomaly Detection Alerts & User Feedback Loop
- **Actor:** Authenticated User
- **Preconditions:** Transaction logged that deviates significantly from user's historical profile.
- **Main Success Scenario:**
  1. Upon logging a transaction ($850 dinner when average dining spend is $45), backend invokes AI anomaly scorer.
  2. Isolation Forest model outputs an anomaly score below the decision threshold (`score < -0.15`).
  3. Backend flags transaction as `is_anomaly = true` and generates an `ai_insights` record.
  4. Dashboard displays an "Unusual Spending Detected" banner with details: "This transaction is 18x higher than your typical Dining expense."
  5. User can click "Mark as Expected" (whitelisting the pattern) or "Review Transaction" to edit/adjust.
- **Postconditions:** User notified of statistical spending anomaly without disruption or false fraud accusations.

---

### UC-08: Review Explainable Financial Optimization Recommendations
- **Actor:** Authenticated User
- **Preconditions:** At least 30 days of income and expense history.
- **Main Success Scenario:**
  1. User navigates to `/recommendations`.
  2. Spring Boot aggregates monthly category percentages, debt-to-savings ratios, and discretionary spending ratios.
  3. Rule engine evaluates financial heuristics (e.g., 50/30/20 guideline, recurring subscription audits).
  4. System surfaces structured recommendations:
     - "High Discretionary Spending: You spent 38% of your income on dining and entertainment. Reducing this to 25% could save you $340/month."
     - "Subscription Audit: 3 recurring streaming services detected totaling $45/month with low weekend engagement."
  5. User can click "Apply to Budget" to automatically adjust category budget caps.
- **Postconditions:** Explainable recommendations delivered with direct action shortcuts.

---

### UC-09: Export Financial Ledger & Reports to CSV
- **Actor:** Authenticated User
- **Preconditions:** Transactions exist.
- **Main Success Scenario:**
  1. User navigates to `/reports` and applies desired date range.
  2. User clicks "Export CSV".
  3. Frontend sends `GET /api/v1/reports/export/csv?startDate=...&endDate=...`.
  4. Spring Boot streams formatted CSV data using HTTP chunked transfer encoding.
  5. Browser receives stream and initiates file download `expense-tracker-report-2026-11.csv`.
- **Postconditions:** User receives complete offline audit trail.

---

### UC-10: Create and Track Financial Savings Goals
- **Actor:** Authenticated User
- **Preconditions:** User is logged in.
- **Main Success Scenario:**
  1. User navigates to `/goals` and clicks "New Goal".
  2. User inputs Goal Name ("Emergency Fund"), Target Amount ($5,000), and Target Date (12 months ahead).
  3. System calculates required monthly contribution ($416.67/month).
  4. When income is saved or manual deposits recorded, progress bar updates.
  5. Visual indicators reflect status: "On Track" (green) or "Behind Target" (orange).
- **Postconditions:** Financial goal created and actively tracked.
