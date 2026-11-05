import React, { useState, useEffect, useCallback } from 'react';
import {
  DollarSign,
  TrendingUp,
  CreditCard,
  PiggyBank,
  Plus,
  Receipt,
  Sparkles,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { StatCard } from '../../components/common/StatCard';
import { Button } from '../../components/common/Button';
import { CardSkeleton } from '../../components/common/SkeletonLoader';
import { EmptyState } from '../../components/common/EmptyState';
import { MonthlySpendingChart } from '../../components/dashboard/MonthlySpendingChart';
import { CategoryBreakdownChart } from '../../components/dashboard/CategoryBreakdownChart';
import { RecentTransactionsTable } from '../../components/dashboard/RecentTransactionsTable';
import { AddEditTransactionModal } from '../../components/transactions/AddEditTransactionModal';
import { dashboardService } from '../../services/dashboardService';
import { categoryService } from '../../services/categoryService';
import { formatCurrency } from '../../utils/currencyFormatter';

export const DashboardPage = () => {
  const { user } = useAuth();
  const { error: toastError } = useToast();
  const currency = user?.preferredCurrency || 'USD';

  const [isLoading, setIsLoading] = useState(true);
  const [summary, setSummary] = useState(null);
  const [spendingTrend, setSpendingTrend] = useState([]);
  const [categoryBreakdown, setCategoryBreakdown] = useState([]);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  const fetchDashboardData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [sumRes, trendRes, catRes, recentRes, allCatsRes] = await Promise.all([
        dashboardService.getDashboardSummary(),
        dashboardService.getSpendingTrend(6),
        dashboardService.getCategoryBreakdown(),
        dashboardService.getRecentTransactions(5),
        categoryService.getCategories(),
      ]);

      setSummary(sumRes?.data || sumRes || null);

      // Map spending trend to chart structure
      const trendData = trendRes?.data || trendRes || [];
      const formattedTrends = Array.isArray(trendData)
        ? trendData.map((t) => ({
            month: t.period,
            income: parseFloat(t.income) || 0,
            expenses: parseFloat(t.expenses) || 0,
          }))
        : [];
      setSpendingTrend(formattedTrends);

      // Map category breakdown to chart structure
      const catData = catRes?.data || catRes || [];
      const formattedCategories = Array.isArray(catData)
        ? catData.map((c) => ({
            category: c.categoryName,
            amount: parseFloat(c.amount) || 0,
          }))
        : [];
      setCategoryBreakdown(formattedCategories);

      // Recent transactions
      const recentData = recentRes?.data || recentRes || [];
      setRecentTransactions(Array.isArray(recentData) ? recentData : []);

      // Categories for quick transaction modal
      const cats = allCatsRes?.data || allCatsRes || [];
      setCategories(Array.isArray(cats) ? cats : []);
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to load dashboard metrics';
      toastError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [toastError]);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  const hasTransactions = summary && summary.transactionCount > 0;

  return (
    <div className="space-y-6">
      {/* Welcome & Action Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Welcome back, {user?.fullName || 'Financial Planner'}!
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Real-time personal finance summary and cashflow overview.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <div className="hidden sm:inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-primary-50 text-primary-700 text-xs font-semibold border border-primary-100">
            <Sparkles className="w-4 h-4 text-primary-600" />
            <span>AI Ready</span>
          </div>

          <Button
            variant="primary"
            size="sm"
            leftIcon={<Plus className="w-4 h-4" />}
            onClick={() => setIsAddModalOpen(true)}
          >
            Add Transaction
          </Button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        {isLoading ? (
          Array.from({ length: 4 }).map((_, i) => <CardSkeleton key={i} />)
        ) : (
          <>
            <StatCard
              title="Total Income"
              value={formatCurrency(summary?.totalIncome || 0, currency)}
              subtitle="current month"
              icon={TrendingUp}
              iconColor="text-emerald-600"
              iconBg="bg-emerald-50"
              trend={{
                direction: 'up',
                label: `${summary?.transactionCount || 0} total records`,
              }}
            />

            <StatCard
              title="Total Expenses"
              value={formatCurrency(summary?.totalExpense || 0, currency)}
              subtitle="current month"
              icon={CreditCard}
              iconColor="text-rose-600"
              iconBg="bg-rose-50"
              trendInverted
            />

            <StatCard
              title="Net Cashflow"
              value={formatCurrency(summary?.netBalance || 0, currency)}
              subtitle="available surplus"
              icon={DollarSign}
              iconColor="text-primary-600"
              iconBg="bg-primary-50"
            />

            <StatCard
              title="Savings Rate"
              value={`${summary?.savingsRatePercent ? Number(summary.savingsRatePercent).toFixed(1) : '0.0'}%`}
              subtitle="of current income"
              icon={PiggyBank}
              iconColor="text-amber-600"
              iconBg="bg-amber-50"
            />
          </>
        )}
      </div>

      {/* When no transactions exist, display helpful empty state */}
      {!isLoading && !hasTransactions && (
        <EmptyState
          icon={Receipt}
          title="No transactions recorded yet"
          description="Your dashboard charts and cashflow analysis will appear automatically once you start logging your transactions."
          action={
            <Button
              variant="primary"
              size="md"
              leftIcon={<Plus className="w-4 h-4" />}
              onClick={() => setIsAddModalOpen(true)}
            >
              Add First Transaction
            </Button>
          }
          className="py-12"
        />
      )}

      {/* Charts Grid */}
      {(!isLoading || hasTransactions) && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          <MonthlySpendingChart data={spendingTrend} currency={currency} />
          <CategoryBreakdownChart data={categoryBreakdown} currency={currency} />
        </div>
      )}

      {/* Recent Transactions Ledger */}
      {(!isLoading || hasTransactions) && (
        <RecentTransactionsTable
          transactions={recentTransactions}
          currency={currency}
          showViewAll
        />
      )}

      {/* Quick Add Transaction Modal */}
      <AddEditTransactionModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSuccess={fetchDashboardData}
        categories={categories}
        currency={currency}
      />
    </div>
  );
};
