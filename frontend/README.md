# Expense Tracker - Frontend Module

AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System.

## Overview
This directory contains the single-page application (SPA) for **Expense Tracker**, built with **React 18**, **Vite**, **Tailwind CSS**, and **React Router v6**. It communicates with the Spring Boot 3.3.4 modular monolithic backend via RESTful APIs.

---

## Technology Stack
- **Framework:** React 18.3.1
- **Build Tool:** Vite 5.4.11
- **Styling:** Tailwind CSS 3.4.15, PostCSS, Autoprefixer
- **Routing:** React Router v6.28.0
- **HTTP Client:** Axios 1.7.9 (configured with JWT bearer interceptors and token refresh loop)
- **Data Visualization:** Recharts 2.13.3
- **Icons:** Lucide React 0.460.0
- **Testing:** Vitest 2.1.5, React Testing Library, JSDOM, Jest-DOM

---

## Directory Structure
```
frontend/
├── public/                 # Static assets
├── src/
│   ├── assets/             # Brand logos and imagery
│   ├── components/         # Reusable UI components
│   │   ├── common/         # Button, Input, Select, Card, StatCard, Badge, Modal, EmptyState, Skeleton
│   │   ├── dashboard/      # MonthlySpendingChart, CategoryBreakdownChart, RecentTransactionsTable
│   │   └── layout/         # AppLayout, Sidebar, Header, MobileNav
│   ├── context/            # AuthContext (JWT session state), ToastContext
│   ├── hooks/              # useAuth, useToast
│   ├── pages/
│   │   ├── auth/           # LoginPage, RegisterPage
│   │   ├── budgets/        # BudgetsPage (Phase 4 placeholder)
│   │   ├── common/         # NotFoundPage (404)
│   │   ├── dashboard/      # DashboardPage (KPI cards, charts, ledger)
│   │   ├── goals/          # GoalsPage (placeholder)
│   │   ├── insights/       # InsightsPage (Phase 5 placeholder)
│   │   ├── profile/        # ProfilePage (User details, change password)
│   │   ├── reports/        # ReportsPage (placeholder)
│   │   └── transactions/   # TransactionsPage (Phase 3 placeholder)
│   ├── routes/             # AppRoutes, ProtectedRoute, PublicRoute
│   ├── services/           # api.js (Axios instance), authService.js
│   ├── styles/             # Global styles
│   ├── test/               # Vitest test suite and mocks
│   │   ├── components/     # Button, Input, StatCard, Badge tests
│   │   ├── context/        # AuthContext tests
│   │   ├── pages/          # LoginPage, RegisterPage tests
│   │   ├── routes/         # ProtectedRoute tests
│   │   ├── setup.js        # Vitest setup & mocks
│   │   └── utils/          # Formatter and validator unit tests
│   ├── utils/              # cn, currencyFormatter, dateFormatter, validators
│   ├── App.jsx             # Top-level application component with providers
│   ├── index.css           # Tailwind base styles and custom utilities
│   └── main.jsx            # React root mount point
├── .env.example            # Environment configuration template
├── eslint.config.js        # ESLint flat config
├── index.html              # HTML entry point
├── package.json            # Node package dependencies and scripts
├── postcss.config.js       # PostCSS configuration
├── tailwind.config.js      # Tailwind design system configuration
└── vite.config.js          # Vite and Vitest configuration with proxy & chunking
```

---

## Key Features Implemented (Phase 2)
1. **Design System & Layout:**
   - Cohesive slate & primary indigo financial palette.
   - Fully responsive application layout with collapsible desktop sidebar, top header, and mobile navigation drawer.
   - Comprehensive set of accessible reusable components: `Button`, `Input`, `Select`, `Card`, `StatCard`, `Badge`, `Modal`, `EmptyState`, `SkeletonLoader`.

2. **Authentication & Session Lifecycle:**
   - Seamless integration with Spring Boot Phase 1 endpoints (`/api/v1/auth/login`, `/api/v1/auth/register`, `/api/v1/auth/refresh`, `/api/v1/users/me`).
   - Secure token handling: in-memory short-lived access token + `sessionStorage` refresh token fallback.
   - Automatic 401 interceptor that queues pending requests and transparently refreshes expired access tokens.
   - Form validation with real-time password strength meter (8+ chars, uppercase, lowercase, digit, special character).

3. **Dashboard Foundation:**
   - KPI metrics: Total Income, Total Expenses, Net Cashflow, Savings Rate with trend indicators.
   - Spending Trend area chart (Income vs Expenses) powered by Recharts.
   - Category Breakdown donut chart with interactive hover tooltips.
   - Recent Transactions ledger table with category badges and positive/negative cashflow indicators.

4. **Testing & QA Automation:**
   - 100% test pass rate across 11 test suites and 50 unit/integration tests using Vitest and React Testing Library.
   - ESLint check configured with zero warnings and zero errors.
   - Code-split production build with vendor chunking under 500 kB.

---

## Development Setup

### Prerequisites
- Node.js 18+ (Node 22 LTS recommended)
- npm 9+
- Backend running on `http://localhost:8080` (optional for mock/isolated frontend work)

### Environment Configuration
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Default configuration:
```env
VITE_API_BASE_URL=/api/v1
```

### Install Dependencies
```bash
npm install
```

### Run Development Server
```bash
npm run dev
```
The application will be accessible at `http://localhost:5173`. Requests to `/api` are automatically proxied to `http://localhost:8080`.

### Run Test Suite
```bash
npm test
```

### Run ESLint
```bash
npm run lint
```

### Build for Production
```bash
npm run build
```
Compiled production bundles are output to `dist/`.
