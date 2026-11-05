import React from 'react';
import {
  DollarSign,
  TrendingUp,
  CreditCard,
  PiggyBank,
  Sparkles,
  Info,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { StatCard } from '../../components/common/StatCard';
import { MonthlySpendingChart } from '../../components/dashboard/MonthlySpendingChart';
import { CategoryBreakdownChart } from '../../components/dashboard/CategoryBreakdownChart';
import { RecentTransactionsTable } from '../../components/dashboard/RecentTransactionsTable';
import { formatCurrency } from '../../utils/currencyFormatter';

// Demonstration data illustrating Phase 2 layout capabilities until Phase 3 transaction APIs are wired
const DEMO_MONTHLY_DATA = [
  { month: 'Jun', income: 4200, expenses: 2600 },
  { month: 'Jul', income: 4350, expenses: 2850 },
  { month: 'Aug', income: 4400, expenses: 2700 },
  { month: 'Sep', income: 4600, expenses: 3100 },
  { month: 'Oct', income: 4500, expenses: 2950 },
  { month: 'Nov', income: 4800, expenses: 2800 },
];

const DEMO_CATEGORIES_DATA = [
  { category: 'Housing & Rent', amount: 1200 },
  { category: 'Groceries & Food', amount: 550 },
  { category: 'Transportation', amount: 320 },
  { category: 'Utilities & Bills', amount: 280 },
  { category: 'Entertainment', amount: 210 },
  { category: 'Healthcare', amount: 140 },
  { category: 'Miscellaneous', amount: 100 },
];

const DEMO_RECENT_TRANSACTIONS = [
  {
    id: 'tx-1',
    description: 'Tech Corp Payroll Deposit',
    categoryName: 'Salary',
    type: 'INCOME',
    amount: 4800.0,
    transactionDate: new Date().toISOString(),
    paymentMethod: 'Direct Deposit',
  },
  {
    id: 'tx-2',
    description: 'Whole Foods Market',
    categoryName: 'Groceries & Food',
    type: 'EXPENSE',
    amount: 142.5,
    transactionDate: new Date(Date.now() - 86400000 * 1).toISOString(),
    paymentMethod: 'Credit Card',
  },
  {
    id: 'tx-3',
    description: 'Metropolitan Apartment Rent',
    categoryName: 'Housing & Rent',
    type: 'EXPENSE',
    amount: 1200.0,
    transactionDate: new Date(Date.now() - 86400000 * 2).toISOString(),
    paymentMethod: 'Bank Transfer',
  },
  {
    id: 'tx-4',
    description: 'Freelance Design Consultation',
    categoryName: 'Freelance',
    type: 'INCOME',
    amount: 650.0,
    transactionDate: new Date(Date.now() - 86400000 * 3).toISOString(),
    paymentMethod: 'Electronic Transfer',
  },
  {
    id: 'tx-5',
    description: 'City Energy Utility Bill',
    categoryName: 'Utilities & Bills',
    type: 'EXPENSE',
    amount: 115.3,
    transactionDate: new Date(Date.now() - 86400000 * 4).toISOString(),
    paymentMethod: 'Auto Debit',
  },
];

export const DashboardPage = () => {
  const { user } = useAuth();
  const currency = user?.preferredCurrency || 'USD';

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Welcome back, {user?.fullName || 'Financial Planner'}!
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Here is your financial performance summary and cashflow overview.
          </p>
        </div>

        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-xl bg-primary-50 text-primary-700 text-xs font-semibold border border-primary-100 self-start sm:self-auto">
          <Sparkles className="w-4 h-4 text-primary-600" />
          <span>AI Forecasting: Active</span>
        </div>
      </div>

      {/* Phase 2 Notification Notice */}
      <div className="p-4 rounded-xl bg-blue-50/80 border border-blue-200/80 flex items-start gap-3 text-xs text-blue-900">
        <Info className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
        <div>
          <span className="font-semibold">Phase 2 Dashboard Foundation: </span>
          Authentication is verified and live against Spring Boot 3.3.4. Transaction ledger and category metrics currently display demonstration data until the Phase 3 backend CRUD endpoints are integrated.
        </div>
      </div>

      {/* KPI Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <StatCard
          title="Total Income"
          value={formatCurrency(5450, currency)}
          subtitle="this month"
          icon={TrendingUp}
          iconColor="text-emerald-600"
          iconBg="bg-emerald-50"
          trend={{ direction: 'up', label: '+8.4% vs last mo' }}
        />

        <StatCard
          title="Total Expenses"
          value={formatCurrency(2800, currency)}
          subtitle="this month"
          icon={CreditCard}
          iconColor="text-rose-600"
          iconBg="bg-rose-50"
          trend={{ direction: 'down', label: '-3.2% vs last mo' }}
          trendInverted
        />

        <StatCard
          title="Net Cashflow"
          value={formatCurrency(2650, currency)}
          subtitle="available surplus"
          icon={DollarSign}
          iconColor="text-primary-600"
          iconBg="bg-primary-50"
          trend={{ direction: 'up', label: '+14.2% surplus' }}
        />

        <StatCard
          title="Savings Rate"
          value="48.6%"
          subtitle="target: 40%"
          icon={PiggyBank}
          iconColor="text-amber-600"
          iconBg="bg-amber-50"
          trend={{ direction: 'up', label: '+4.1% above goal' }}
        />
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <MonthlySpendingChart data={DEMO_MONTHLY_DATA} currency={currency} />
        <CategoryBreakdownChart data={DEMO_CATEGORIES_DATA} currency={currency} />
      </div>

      {/* Recent Transactions Table */}
      <RecentTransactionsTable
        transactions={DEMO_RECENT_TRANSACTIONS}
        currency={currency}
      />
    </div>
  );
};

