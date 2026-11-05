# REST API Specification

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  
**Base URL:** `https://api.expensetracker.local/api/v1` (Production: `/api/v1`)  

---

## 1. Global Conventions & Standards

- **Protocols:** HTTPS, TLS 1.3
- **Data Exchange Format:** `application/json; charset=UTF-8`
- **Authentication Scheme:** `Authorization: Bearer <JWT_ACCESS_TOKEN>`
- **Monetary Representation:** Strings or fixed two-decimal numbers (e.g. `124.50`)
- **Timestamps:** ISO 8601 UTC (e.g. `2026-11-05T09:30:00Z`)

### Standard Error Response Format (RFC 7807)
```json
{
  "timestamp": "2026-11-05T09:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Field 'amount' must be greater than 0",
  "path": "/api/v1/transactions",
  "validationErrors": [
    {
      "field": "amount",
      "rejectedValue": -50.00,
      "message": "Amount must be strictly positive"
    }
  ]
}
```

---

## 2. Authentication Endpoints (`/api/v1/auth`)

### 2.1 Register User
- **Method & Path:** `POST /api/v1/auth/register`
- **Auth Required:** No (Public)
- **Request Body:**
```json
{
  "fullName": "Purushottam Sharma",
  "email": "purush@example.com",
  "password": "Password123#",
  "currency": "USD"
}
```
- **Response `201 Created`:**
```json
{
  "userId": "d7a4b8c2-3e2a-4a6f-b258-0123456789ab",
  "fullName": "Purushottam Sharma",
  "email": "purush@example.com",
  "currency": "USD",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d8f4c1e0-4a8b-4b8c-9c9d-abcdef012345",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### 2.2 Login User
- **Method & Path:** `POST /api/v1/auth/login`
- **Auth Required:** No (Public)
- **Request Body:**
```json
{
  "email": "purush@example.com",
  "password": "Password123#"
}
```
- **Response `200 OK`:** (Returns JWT access token and refresh token)

### 2.3 Refresh Token
- **Method & Path:** `POST /api/v1/auth/refresh`
- **Auth Required:** No (Validated via refresh token payload)
- **Request Body:**
```json
{
  "refreshToken": "d8f4c1e0-4a8b-4b8c-9c9d-abcdef012345"
}
```
- **Response `200 OK`:** New `accessToken` and updated `expiresIn`.

### 2.4 Current User Profile
- **Method & Path:** `GET /api/v1/auth/me`
- **Auth Required:** Yes (`ROLE_USER`)
- **Response `200 OK`:** Profile details and account preferences.

---

## 3. Transaction Management (`/api/v1/transactions`)

### 3.1 List / Search Transactions (Paginated)
- **Method & Path:** `GET /api/v1/transactions`
- **Auth Required:** Yes
- **Query Parameters:**
  - `page`: Integer (default 0)
  - `size`: Integer (default 20, max 100)
  - `startDate`: ISO Date (`YYYY-MM-DD`)
  - `endDate`: ISO Date (`YYYY-MM-DD`)
  - `type`: Enum (`INCOME` | `EXPENSE`)
  - `categoryId`: UUID
  - `minAmount`: Decimal
  - `maxAmount`: Decimal
  - `query`: String (search merchant & description)
  - `sortBy`: String (`transactionDate`, `amount`, `createdAt`)
  - `sortDirection`: String (`ASC`, `DESC`)
- **Response `200 OK`:**
```json
{
  "content": [
    {
      "id": "c1f8d4e2-9b2a-4a6f-b258-112233445566",
      "amount": 84.50,
      "type": "EXPENSE",
      "transactionDate": "2026-11-04T18:22:00Z",
      "description": "Weekly grocery supplies",
      "merchant": "Whole Foods Market",
      "paymentMethod": "CARD",
      "category": {
        "id": "e3b0c442-98fc-1c14-9afbf4c8996fb924",
        "name": "Groceries",
        "colorHex": "#10B981",
        "iconName": "ShoppingCart"
      },
      "isRecurring": false,
      "isAnomaly": false,
      "aiCategorized": true
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 142,
  "totalPages": 8,
  "isLast": false
}
```

### 3.2 Create Transaction
- **Method & Path:** `POST /api/v1/transactions`
- **Auth Required:** Yes
- **Request Body:**
```json
{
  "amount": 84.50,
  "type": "EXPENSE",
  "categoryId": "e3b0c442-98fc-1c14-9afbf4c8996fb924",
  "transactionDate": "2026-11-04T18:22:00Z",
  "description": "Weekly grocery supplies",
  "merchant": "Whole Foods Market",
  "paymentMethod": "CARD",
  "notes": "Purchased organic produce",
  "aiCategorized": true
}
```
- **Response `201 Created`:** Created transaction entity.

### 3.3 Update & Delete Transaction
- **Method & Path:** `PUT /api/v1/transactions/{id}` -> Updates transaction.
- **Method & Path:** `DELETE /api/v1/transactions/{id}` -> Deletes transaction (`204 No Content`).

---

## 4. Category Management (`/api/v1/categories`)

- `GET /api/v1/categories`: Retrieve all categories (system defaults + user-specific).
- `POST /api/v1/categories`: Create custom category.
- `PUT /api/v1/categories/{id}`: Edit category label, color, or icon.
- `DELETE /api/v1/categories/{id}`: Delete category (validating no active linked records).

---

## 5. Dashboard & Analytics (`/api/v1/dashboard`)

### 5.1 Financial Summary KPI
- **Method & Path:** `GET /api/v1/dashboard/summary?year=2026&month=11`
- **Response `200 OK`:**
```json
{
  "period": "2026-11",
  "currency": "USD",
  "totalIncome": 6500.00,
  "totalExpense": 3420.50,
  "netSavings": 3079.50,
  "savingsRatePercent": 47.38,
  "previousMonthExpense": 3810.00,
  "monthOverMonthExpenseChangePercent": -10.22
}
```

### 5.2 Category Breakdown
- **Method & Path:** `GET /api/v1/dashboard/category-breakdown?year=2026&month=11`
- **Response `200 OK`:** Breakdown of spend by category with amounts and percentages.

### 5.3 Daily Spending Trend
- **Method & Path:** `GET /api/v1/dashboard/spending-trend?days=30`
- **Response `200 OK`:** Day-by-day expenditure array for chart rendering.

---

## 6. Budget Management (`/api/v1/budgets`)

- `GET /api/v1/budgets?year=2026&month=11`: List all budgets for month.
- `POST /api/v1/budgets`: Set overall or category budget envelope.
- `GET /api/v1/budgets/status?year=2026&month=11`: Detailed progress with remaining balance, percentage spent, and overrun warning status.

---

## 7. Financial Goals (`/api/v1/goals`)

- `GET /api/v1/goals`: List all active savings goals.
- `POST /api/v1/goals`: Create new savings milestone.
- `POST /api/v1/goals/{id}/contribute`: Log contribution toward goal.

---

## 8. Reports & Data Export (`/api/v1/reports`)

- `GET /api/v1/reports/income-vs-expense?months=12`: 12-month rolling income vs expense comparison.
- `GET /api/v1/reports/export/csv`: Streams CSV export of transaction ledger.

---

## 9. AI Integration Endpoints (`/api/v1/ai`)

### 9.1 Transaction Auto-Categorization
- **Method & Path:** `POST /api/v1/ai/categorize`
- **Request Body:**
```json
{
  "description": "Starbucks Reserve Roast",
  "merchant": "Starbucks",
  "amount": 7.45
}
```
- **Response `200 OK`:**
```json
{
  "suggestedCategoryId": "a1b2c3d4-0000-1111-2222-333344445555",
  "suggestedCategoryName": "Dining & Coffee",
  "confidence": 0.945,
  "requiresConfirmation": false,
  "alternativeSuggestions": [
    { "categoryName": "Groceries", "confidence": 0.042 }
  ]
}
```

### 9.2 Expense Forecasting
- **Method & Path:** `GET /api/v1/ai/forecast?horizonDays=30`
- **Response `200 OK`:**
```json
{
  "horizonDays": 30,
  "projectedTotal": 3250.00,
  "dailyProjections": [
    { "date": "2026-11-06", "predictedAmount": 105.40, "lowerBound": 85.00, "upperBound": 125.80 }
  ],
  "modelVersion": "xgboost-ts-v1.0",
  "confidenceScore": 0.88,
  "isColdStart": false
}
```

### 9.3 Budget Overrun Risk
- **Method & Path:** `GET /api/v1/ai/overrun-check?year=2026&month=11`
- **Response `200 OK`:** Status on whether month-end spend is predicted to breach budget caps.

### 9.4 Recommendations & Anomalies
- `GET /api/v1/ai/insights`: List actionable suggestions and unusual transaction flags.
- `POST /api/v1/ai/insights/{id}/dismiss`: Dismiss insight card.
