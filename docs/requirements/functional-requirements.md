# Functional Requirements Specification

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Authentication & User Identity Management (FR-AUTH)

| Req ID | Feature / Capability | Description & Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| **FR-AUTH-01** | User Registration | System must permit new users to register providing full name, unique valid email, password, and preferred primary currency (USD, EUR, GBP, INR, etc.). Passwords must enforce minimum 8 characters, with at least 1 uppercase letter, 1 number, and 1 special symbol. | High |
| **FR-AUTH-02** | User Login | System must authenticate users via email and password, returning an access token (JWT, 15m expiration) and a refresh token (7d expiration). Failed attempts must return generic "Invalid credentials" error to prevent email enumeration. | High |
| **FR-AUTH-03** | Token Refresh | System must exchange a valid refresh token for a new short-lived access token without forcing the user to re-authenticate manually. | High |
| **FR-AUTH-04** | User Logout | System must invalidate the client-side session and revoke the active refresh token in the backend revocation store. | High |
| **FR-AUTH-05** | Profile Management | Users can view and update their profile details (name, preferred currency, monthly income baseline) and change their password by supplying their current password. | Medium |
| **FR-AUTH-06** | Data Isolation Guard | The system must guarantee that authenticated users can only query, modify, or delete their own data records. Multi-tenant access violations must throw an HTTP 403 Forbidden. | Critical |

---

## 2. Category Management (FR-CAT)

| Req ID | Feature / Capability | Description & Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| **FR-CAT-01** | Default System Taxonomy | The system must initialize predefined standard categories for every user (e.g., Housing, Groceries, Utilities, Dining, Transportation, Healthcare, Entertainment, Salary, Investments, Freelance). | High |
| **FR-CAT-02** | Custom Categories | Users can create custom categories specifying category name, transaction type (`EXPENSE` or `INCOME`), custom color code (hex), and an icon identifier. | High |
| **FR-CAT-03** | Duplicate Prevention | Category names must be unique per user and transaction type (case-insensitive). | Medium |
| **FR-CAT-04** | Category Deletion / Archival | When a user deletes a category that already has linked transactions, the system must either prevent deletion or reassign existing transactions to an "Uncategorized" category. | High |

---

## 3. Transaction Management (FR-TXN)

| Req ID | Feature / Capability | Description & Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| **FR-TXN-01** | Record Income & Expense | Users can create transactions with: amount (positive `NUMERIC(15,2)`), type (`INCOME` or `EXPENSE`), category ID, transaction timestamp, description, merchant/payee name, and optional notes/tags. | High |
| **FR-TXN-02** | Edit & Delete | Users can update any transaction attribute or delete transactions. Updates immediately recalculate balances, budget statuses, and analytical reports. | High |
| **FR-TXN-03** | Paginated Ledger | Transactions must be returned using server-side pagination (default 20 records per page) with total count, total pages, and navigation metadata. | High |
| **FR-TXN-04** | Multi-Criteria Filtering | Users can filter transactions simultaneously by: Date range (`startDate`, `endDate`), Category ID list, Transaction Type (`INCOME`, `EXPENSE`), and Amount range (`minAmount`, `maxAmount`). | High |
| **FR-TXN-05** | Full-Text Search | System must allow instant case-insensitive substring searching across transaction descriptions, merchant names, and notes. | Medium |
| **FR-TXN-06** | Sorting | System supports sorting by `transactionDate`, `amount`, `createdAt`, or `categoryName` in ascending (`ASC`) or descending (`DESC`) order. | Medium |
| **FR-TXN-07** | Recurring Transactions | Users can configure recurring transaction templates (Daily, Weekly, Bi-weekly, Monthly, Yearly). The system generates or flags due transactions on their scheduled dates. | Medium |

---

## 4. Financial Dashboard (FR-DASH)

| Req ID | Feature / Capability | Description & Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| **FR-DASH-01** | Summary KPI Metrics | The dashboard must display real-time calculated financial aggregates: Total Income, Total Expenses, Net Savings Balance, and Savings Rate percentage for the selected period. | High |
| **FR-DASH-02** | Monthly Spending Curve | Interactive line chart illustrating cumulative day-by-day spending for the current month compared with the prior month. | High |
| **FR-DASH-03** | Category Breakdown | Interactive donut/pie chart displaying expense distribution across categories with percentage allocations and drill-down tooltips. | High |
| **FR-DASH-04** | Recent Ledger Feed | Quick list of the 5 to 10 most recent transactions with icons, category badges, formatted timestamps, and clear positive/negative amount indicators. | High |
| **FR-DASH-05** | Quick-Action Controls | Direct modal triggers for adding income, adding expenses, or creating budgets directly from the main view. | High |

---

## 5. Budget Management & Overrun Engine (FR-BUD)

| Req ID | Feature / Capability | Description & Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| **FR-BUD-01** | Monthly Overall Budget | Users can set an aggregate spending limit for any given calendar month. | High |
| **FR-BUD-02** | Category-Specific Budgets | Users can allocate distinct budget limits to individual expense categories for a target month. | High |
| **FR-BUD-03** | Real-Time Burn Rate | System calculates and displays: Current spend, Remaining budget balance, Percentage consumed, and Days remaining in the cycle. | High |
| **FR-BUD-04** | Threshold Alerts | Visual and system status warnings must trigger when budget consumption exceeds 80% (Warning) and 100% (Breached). | High |
| **FR-BUD-05** | Predictive Overrun Alert | Integrating with Module AI-OVR, the system predicts whether current spending velocity will breach the budget before month end and alerts the user with estimated overrun magnitude. | High |

---

## 6. Financial Goals Tracking (FR-GOAL)

| Req ID | Feature / Capability | Description & Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| **FR-GOAL-01** | Savings Goals Creation | Users can establish target savings goals with goal title, target amount, target completion date, and optional category link. | Medium |
| **FR-GOAL-02** | Manual & Linked Deposits | Users can log contributions toward specific goals, updating current saved balance and percentage progress. | Medium |
| **FR-GOAL-03** | Status & Velocity Tracking | System displays whether the goal is On Track, Ahead of Schedule, or Behind Schedule based on days elapsed versus funds saved. | Medium |

---

## 7. Reports & Data Export (FR-RPT)

| Req ID | Feature / Capability | Description & Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| **FR-RPT-01** | Granular Periodic Reports | Users can generate Daily, Weekly, Monthly, and Annual summary reports showing income, expense, net cash flow, and top expenditure drivers. | High |
| **FR-RPT-02** | Income vs Expense Analysis | Comparative bar/area visualization contrasting incoming revenues with outgoing liabilities over rolling 6 and 12-month periods. | High |
| **FR-RPT-03** | CSV Export Streaming | System must stream a CSV file containing all filtered transactions with standard headers: Transaction ID, Date, Type, Category, Amount, Currency, Merchant, Description, and Notes. | High |

---

## 8. AI & Machine Learning Capabilities (FR-AI)

| Req ID | Module & Feature | Description & Acceptance Criteria | Priority |
| :--- | :--- | :--- | :--- |
| **FR-AI-01** | Transaction Categorization (Module 1) | Given a transaction description and merchant name, the AI service classifies the entry into a standard category using a trained TF-IDF + Logistic Regression model. | High |
| **FR-AI-02** | Prediction Confidence | Every categorization output must return a calibrated confidence probability (`0.0` to `1.0`). Predictions with confidence below threshold (e.g., `< 0.60`) must flag `requires_user_confirmation = true`. | High |
| **FR-AI-03** | Expense Forecasting (Module 2) | System aggregates daily historical expenses and forecasts projected spend for the next 7, 14, and 30 days using an XGBoost regression model with rolling temporal features. | High |
| **FR-AI-04** | Forecast Cold-Start Handling | For accounts with fewer than 30 historical transactions or under 14 active days, the model falls back to a deterministic moving average with explicit confidence limitation notes. | High |
| **FR-AI-05** | Budget Overrun Prediction (Module 3) | Combines actual current-month spend with forecasted spend for the remaining month to estimate final month-end total and emit overrun probabilities. | High |
| **FR-AI-06** | Anomaly Detection (Module 4) | An Isolation Forest model identifies anomalous transactions based on amount deviation, unusual time-of-day/day-of-week, and category spending distribution. Flagged records are presented as "Unusual Spend" without declaring fraud. | Medium |
| **FR-AI-07** | Financial Recommendations (Module 5) | Heuristic and statistical recommendation engine identifies disproportionate spending categories (e.g. >35% of income on discretionary items) and provides actionable, explainable suggestions. | Medium |
