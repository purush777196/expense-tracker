# Frontend Architecture Specification

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Architectural Overview & Rationale

The **Expense Tracker** client application is engineered as a responsive Single-Page Application (SPA) leveraging **React 18** and **Vite**, styled using utility-first **Tailwind CSS**, and rendered with rich financial data visualizations through **Recharts**.

### Core Technical Pillars:
1. **Component-Driven Modular Design:** Clean separation between presentational atomic components, structured compound layouts, and smart container hooks.
2. **Predictable State Lifecycle:** Authentication and application-level preferences are governed via standard React Context API; server data interactions are isolated into typed API service modules and custom lifecycle hooks (`useTransactions`, `useDashboard`, `useBudgets`).
3. **Resilient Network Client:** An Axios client configured with interceptors handles automated JWT injection, 401 token refresh loops, and RFC 7807 problem details parsing.
4. **Accessible Financial Visualizations:** Visual spending charts built with Recharts support responsive SVG scaling, high-contrast palette compliance, and screen-reader accessible data tables.

---

## 2. Directory Structure

```
frontend/src/
├── assets/                  # Static assets (images, SVGs, brand logos)
├── components/              # Reusable UI component library
│   ├── common/              # Buttons, Cards, Inputs, Modals, Badges, Loaders
│   │   ├── Button.jsx
│   │   ├── Card.jsx
│   │   ├── Input.jsx
│   │   ├── Select.jsx
│   │   ├── Modal.jsx
│   │   ├── Badge.jsx
│   │   ├── StatCard.jsx
│   │   ├── SkeletonLoader.jsx
│   │   └── EmptyState.jsx
│   ├── charts/              # Visualizations wrapped in responsive containers
│   │   ├── SpendingTrendChart.jsx
│   │   ├── CategoryDonutChart.jsx
│   │   ├── IncomeExpenseBarChart.jsx
│   │   └── ForecastAreaChart.jsx
│   ├── layout/              # Application framing and navigation
│   │   ├── AppLayout.jsx
│   │   ├── Sidebar.jsx
│   │   ├── Header.jsx
│   │   └── ProtectedRoute.jsx
│   └── modals/              # Flow-specific dialogs
│       ├── AddTransactionModal.jsx
│       ├── SetBudgetModal.jsx
│       └── CreateGoalModal.jsx
├── context/                 # Global state providers
│   ├── AuthContext.jsx      # Authentication session and token lifecycle
│   └── ToastContext.jsx     # Global alert toast notifications
├── hooks/                   # Custom reusable React hooks
│   ├── useTransactions.js
│   ├── useDashboard.js
│   ├── useBudgets.js
│   ├── useGoals.js
│   └── useDebounce.js
├── pages/                   # Route-level view containers
│   ├── auth/
│   │   ├── LoginPage.jsx
│   │   └── RegisterPage.jsx
│   ├── DashboardPage.jsx
│   ├── TransactionsPage.jsx
│   ├── BudgetsPage.jsx
│   ├── GoalsPage.jsx
│   ├── ReportsPage.jsx
│   ├── RecommendationsPage.jsx
│   └── SettingsPage.jsx
├── services/                # Axios API communication clients
│   ├── api.js               # Master Axios instance with interceptors
│   ├── authService.js
│   ├── transactionService.js
│   ├── categoryService.js
│   ├── budgetService.js
│   ├── reportService.js
│   └── aiService.js
├── utils/                   # Formatting utilities
│   ├── currencyFormatter.js
│   ├── dateFormatter.js
│   └── validators.js
├── App.jsx                  # Root router declarations
├── index.css                # Tailwind directives and custom utility classes
└── main.jsx                 # Vite application entry point
```

---

## 3. Route Hierarchy & Navigation

```
/ (Redirects to /dashboard or /login based on auth status)
├── /login                   [Public] Email/password login
├── /register                [Public] Account signup
│
└── [Protected Routes Layout - AppLayout]
    ├── /dashboard           [Protected] Primary financial cockpit & KPI metrics
    ├── /transactions        [Protected] Searchable, filterable ledger & pagination
    ├── /budgets             [Protected] Monthly envelopes & overrun risk monitors
    ├── /goals               [Protected] Savings targets & contribution tracking
    ├── /reports             [Protected] Periodic analysis & CSV streaming export
    ├── /recommendations     [Protected] Explainable AI insights & anomaly alerts
    └── /settings            [Protected] Profile info & currency preferences
```

---

## 4. UI Design System & Component Library

- **Color Palette:**
  - Primary Brand: Deep Indigo (`#4F46E5` / Tailwind `indigo-600`)
  - Accent / Positive Cash Flow: Emerald Green (`#10B981` / Tailwind `emerald-500`)
  - Alert / Negative Cash Flow: Rose Red (`#F43F5E` / Tailwind `rose-500`)
  - Warning / Overrun Risk: Amber (`#F59E0B` / Tailwind `amber-500`)
  - Background Neutral: Slate Gray (`#F8FAFC` to `#0F172A`)
- **Typography:** Inter (`sans-serif`) with strict hierarchical scale (`text-xs` through `text-3xl`).
- **Feedback & States:** Every interactive card and table implements three distinct lifecycle states:
  1. *Loading:* Tailwind pulse animated Skeleton loader mimicking exact component geometry.
  2. *Empty:* Semantic icon, helpful headline, and direct Call to Action button.
  3. *Error:* Clear alert banner with retry trigger.

---

## 5. Security & Token Lifecycle Handling

The Axios instance in `services/api.js` enforces strict token lifecycle management:
1. **Request Interceptor:** Automatically pulls the current JWT access token from memory and sets `Authorization: Bearer <token>`.
2. **Response Interceptor:** Traps HTTP 401 Unauthorized errors:
   - Queues pending requests while attempting a token refresh via `/auth/refresh`.
   - On successful refresh, replays the original requests transparently.
   - If the refresh token is also invalid or expired, clears stored credentials and redirects to `/login`.
3. **XSS & Content Sanitization:** All user-supplied strings are escaped prior to rendering.
