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
- **Method & Path:** `GET /api/v1/auth/me` (alias: `GET /api/v1/users/me`)
- **Auth Required:** Yes (`ROLE_USER`)
- **Response `200 OK`:** Profile details and account preferences.
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": "d7a4b8c2-3e2a-4a6f-b258-0123456789ab",
    "fullName": "Purushottam Sharma",
    "email": "purush@example.com",
    "currency": "USD",
    "role": "ROLE_USER",
    "isActive": true,
    "createdAt": "2026-11-05T09:30:00Z",
    "updatedAt": "2026-11-05T09:30:00Z"
  },
  "timestamp": "2026-11-05T09:35:00Z"
}
```

### 2.5 Update User Profile
- **Method & Path:** `PUT /api/v1/users/me`
- **Auth Required:** Yes (`ROLE_USER`)
- **Request Body:**
```json
{
  "fullName": "Purushottam Sharma",
  "currency": "EUR"
}
```
- **Response `200 OK`:** Updated user profile.

### 2.6 Change Password
- **Method & Path:** `PUT /api/v1/users/me/password`
- **Auth Required:** Yes (`ROLE_USER`)
- **Request Body:**
```json
{
  "currentPassword": "Password123#",
  "newPassword": "NewSecurePassword456#"
}
```
- **Response `200 OK`:** Success message.

---

## 3. Transaction Management (`/api/v1/transactions`)

### 3.1 List / Search Transactions (Paginated)
- **Method & Path:** `GET /api/v1/transactions`
- **Auth Required:** Yes (`ROLE_USER`)
- **Query Parameters:**
  - `page`: Integer (default 0)
  - `size`: Integer (default 20, max 100)
  - `startDate`: ISO-8601 Instant (`2026-11-01T00:00:00Z`)
  - `endDate`: ISO-8601 Instant (`2026-11-30T23:59:59Z`)
  - `type`: Enum (`INCOME` | `EXPENSE`)
  - `categoryId`: UUID
  - `minAmount`: Decimal
  - `maxAmount`: Decimal
  - `query`: String (case-insensitive search in description and notes)
  - `sortBy`: String (`transactionDate`, `amount`, `createdAt` - default `transactionDate`)
  - `sortDirection`: String (`ASC`, `DESC` - default `DESC`)
- **Response `200 OK`:**
```json
{
  "content": [
    {
      "id": "c1f8d4e2-9b2a-4a6f-b258-112233445566",
      "userId": "d7a4b8c2-3e2a-4a6f-b258-0123456789ab",
      "category": {
        "id": "e3b0c442-98fc-1c14-9afbf4c8996fb924",
        "name": "Groceries",
        "type": "EXPENSE",
        "colorHex": "#10B981",
        "iconName": "ShoppingCart",
        "isSystemDefault": true,
        "userId": null
      },
      "type": "EXPENSE",
      "amount": 84.50,
      "description": "Weekly grocery supplies",
      "transactionDate": "2026-11-04T18:22:00Z",
      "paymentMethod": "CARD",
      "notes": "Purchased organic produce",
      "createdAt": "2026-11-04T18:22:15Z",
      "updatedAt": "2026-11-04T18:22:15Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1,
  "isFirst": true,
  "isLast": true,
  "hasNext": false,
  "hasPrevious": false
}
```

### 3.2 Create Transaction
- **Method & Path:** `POST /api/v1/transactions`
- **Auth Required:** Yes (`ROLE_USER`)
- **Request Body:**
```json
{
  "categoryId": "e3b0c442-98fc-1c14-9afbf4c8996fb924",
  "amount": 84.50,
  "type": "EXPENSE",
  "transactionDate": "2026-11-04T18:22:00Z",
  "description": "Weekly grocery supplies",
  "paymentMethod": "CARD",
  "notes": "Purchased organic produce"
}
```
- **Response `201 Created`:** Wrapped `ApiResponse<TransactionResponse>`.

### 3.3 Get Transaction by ID
- **Method & Path:** `GET /api/v1/transactions/{id}`
- **Auth Required:** Yes (`ROLE_USER`)
- **Response `200 OK`:** Wrapped `ApiResponse<TransactionResponse>`.

### 3.4 Update Transaction
- **Method & Path:** `PUT /api/v1/transactions/{id}`
- **Auth Required:** Yes (`ROLE_USER`)
- **Request Body:** Same schema as `POST /api/v1/transactions`.
- **Response `200 OK`:** Wrapped `ApiResponse<TransactionResponse>`.

### 3.5 Delete Transaction
- **Method & Path:** `DELETE /api/v1/transactions/{id}`
- **Auth Required:** Yes (`ROLE_USER`)
- **Response `204 No Content`**

### 3.6 Transaction Aggregated Summary
- **Method & Path:** `GET /api/v1/transactions/summary`
- **Auth Required:** Yes (`ROLE_USER`)
- **Query Parameters:** `startDate` (ISO-8601), `endDate` (ISO-8601)
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "Transaction summary retrieved successfully",
  "data": {
    "totalIncome": 6500.00,
    "totalExpense": 3420.50,
    "netSavings": 3079.50,
    "savingsRatePercent": 47.38,
    "transactionCount": 42
  },
  "timestamp": "2026-11-05T12:00:00Z"
}
```

---

## 4. Category Management (`/api/v1/categories`)

### 4.1 List Categories
- **Method & Path:** `GET /api/v1/categories`
- **Auth Required:** Yes (`ROLE_USER`)
- **Query Parameters:** `type` (optional: `INCOME` | `EXPENSE`)
- **Response `200 OK`:** Returns system default categories combined with user-created custom categories.

### 4.2 Create Custom Category
- **Method & Path:** `POST /api/v1/categories`
- **Auth Required:** Yes (`ROLE_USER`)
- **Request Body:**
```json
{
  "name": "Side Consulting",
  "type": "INCOME",
  "colorHex": "#3B82F6",
  "iconName": "Briefcase"
}
```
- **Response `201 Created`:** Wrapped `ApiResponse<CategoryResponse>`.

### 4.3 Update Custom Category
- **Method & Path:** `PUT /api/v1/categories/{id}`
- **Auth Required:** Yes (`ROLE_USER`)
- **Rules:** Cannot modify system default categories (`isSystemDefault: true` returns `400 Bad Request`).
- **Response `200 OK`:** Wrapped `ApiResponse<CategoryResponse>`.

### 4.4 Delete Custom Category
- **Method & Path:** `DELETE /api/v1/categories/{id}`
- **Auth Required:** Yes (`ROLE_USER`)
- **Rules:**
  - Cannot delete system default categories (`400 Bad Request`).
  - Cannot delete a category that is actively referenced by one or more transactions (`409 Conflict`).
- **Response `204 No Content`**

---

## 5. Dashboard & Analytics (`/api/v1/dashboard`)

### 5.1 Financial Summary KPI
- **Method & Path:** `GET /api/v1/dashboard/summary`
- **Auth Required:** Yes (`ROLE_USER`)
- **Query Parameters:** `year` (int, default current), `month` (1-12, default current)
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "Dashboard summary retrieved successfully",
  "data": {
    "year": 2026,
    "month": 11,
    "totalIncome": 6500.00,
    "totalExpense": 3420.50,
    "netSavings": 3079.50,
    "savingsRatePercent": 47.38,
    "previousMonthExpense": 3810.00,
    "expenseGrowthRatePercent": -10.22,
    "transactionCount": 42
  },
  "timestamp": "2026-11-05T12:00:00Z"
}
```

### 5.2 Category Expense Breakdown
- **Method & Path:** `GET /api/v1/dashboard/category-breakdown`
- **Auth Required:** Yes (`ROLE_USER`)
- **Query Parameters:** `year` (int), `month` (1-12)
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "Category breakdown retrieved successfully",
  "data": [
    {
      "categoryId": "e3b0c442-98fc-1c14-9afbf4c8996fb924",
      "categoryName": "Groceries",
      "colorHex": "#10B981",
      "iconName": "ShoppingCart",
      "totalAmount": 750.00,
      "percentage": 21.93,
      "transactionCount": 12
    }
  ],
  "timestamp": "2026-11-05T12:00:00Z"
}
```

### 5.3 Monthly Spending Trend
- **Method & Path:** `GET /api/v1/dashboard/spending-trend`
- **Auth Required:** Yes (`ROLE_USER`)
- **Query Parameters:** `months` (int, default 6)
- **Response `200 OK`:** Array of monthly income vs. expense aggregations sorted chronologically.

### 5.4 Recent Transactions
- **Method & Path:** `GET /api/v1/dashboard/recent-transactions`
- **Auth Required:** Yes (`ROLE_USER`)
- **Query Parameters:** `limit` (int, default 5, max 20)
- **Response `200 OK`:** Top latest transactions for quick-glance display on the dashboard ledger.

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
