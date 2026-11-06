import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import {
  DollarSign,
  TrendingUp,
  CreditCard,
  PiggyBank,
  Plus,
  Receipt,
  Sparkles,
  Target,
  Repeat,
  AlertTriangle,
  ArrowRight,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { StatCard } from '../../components/common/StatCard';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { CardSkeleton } from '../../components/common/SkeletonLoader';
import { EmptyState } from '../../components/common/EmptyState';
import { MonthlySpendingChart } from '../../components/dashboard/MonthlySpendingChart';
import { CategoryBreakdownChart } from '../../components/dashboard/CategoryBreakdownChart';
import { RecentTransactionsTable } from '../../components/dashboard/RecentTransactionsTable';
import { AddEditTransactionModal } from '../../components/transactions/AddEditTransactionModal';
import { dashboardService } from '../../services/dashboardService';
import { categoryService } from '../../services/categoryService';
import { budgetService } from '../../services/budgetService';
import { goalService } from '../../services/goalService';
import { recurringService } from '../../services/recurringService';
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

  // Phase 4 financial state
  const [budgetOverview, setBudgetOverview] = useState(null);
  const [goalSummary, setGoalSummary] = useState(null);
  const [upcomingRecurring, setUpcomingRecurring] = useState([]);

  const fetchDashboardData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [sumRes, trendRes, catRes, recentRes, allCatsRes, bOverviewRes, gSummaryRes, upcomingRes] =
        await Promise.all([
          dashboardService.getDashboardSummary(),
          dashboardService.getSpendingTrend(6),
          dashboardService.getCategoryBreakdown(),
          dashboardService.getRecentTransactions(5),
          categoryService.getCategories(),
          budgetService.getBudgetOverview().catch(() => null),
          goalService.getGoalSummary().catch(() => null),
          recurringService.getUpcomingRecurring(14).catch(() => []),
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

      // Phase 4 widgets
      setBudgetOverview(bOverviewRes?.data || bOverviewRes || null);
      setGoalSummary(gSummaryRes?.data || gSummaryRes || null);

      const upList = upcomingRes?.data || upcomingRes || [];
      setUpcomingRecurring(Array.isArray(upList) ? upList : []);
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

  // Phase 4 widget calculations
  const totalBudgeted = parseFloat(budgetOverview?.totalBudgeted) || 0;
  const totalSpent = parseFloat(budgetOverview?.totalSpent) || 0;
  const budgetUtilization = parseFloat(budgetOverview?.overallUtilizationPercentage) || 0;
  const isOverBudget = budgetOverview?.overBudgetCount > 0 || budgetUtilization >= 100;

  const totalGoalTarget = parseFloat(goalSummary?.totalTargetAmount) || 0;
  const totalGoalSaved = parseFloat(goalSummary?.totalSavedAmount) || 0;
  const goalProgress = parseFloat(goalSummary?.overallProgressPercentage) || 0;

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

      {/* Phase 4 Financial Health Snapshot Grid */}
      {!isLoading && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
          {/* Budget Widget */}
          <Card className="p-5 flex flex-col justify-between hover:shadow-md transition-shadow">
            <div>
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <div className="p-2 rounded-lg bg-indigo-50 text-indigo-600">
                    <PiggyBank className="w-4 h-4" />
                  </div>
                  <h3 className="text-sm font-bold text-slate-900">Monthly Budget</h3>
                </div>
                <Link
                  to="/budgets"
                  className="text-xs text-primary-600 hover:text-primary-700 font-medium inline-flex items-center gap-1"
                >
                  Manage <ArrowRight className="w-3 h-3" />
                </Link>
              </div>

              {totalBudgeted > 0 ? (
                <div className="space-y-2 mt-2">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-500">
                      Spent: <strong className="text-slate-900">{formatCurrency(totalSpent, currency)}</strong>
                    </span>
                    <span className="text-slate-500">
                      Limit: {formatCurrency(totalBudgeted, currency)}
                    </span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                    <div
                      className={`h-2 rounded-full transition-all duration-500 ${
                        isOverBudget ? 'bg-rose-600' : budgetUtilization >= 80 ? 'bg-amber-500' : 'bg-indigo-600'
                      }`}
                      style={{ width: `${Math.min(budgetUtilization, 100)}%` }}
                    />
                  </div>
                  <div className="flex justify-between text-[11px] text-slate-400">
                    <span>{budgetUtilization.toFixed(0)}% utilized</span>
                    <span className={isOverBudget ? 'text-rose-600 font-medium' : 'text-emerald-600 font-medium'}>
                      {isOverBudget ? 'Over budget limit' : `${formatCurrency(totalBudgeted - totalSpent, currency)} left`}
                    </span>
                  </div>
                </div>
              ) : (
                <p className="text-xs text-slate-500 mt-2">
                  No active budget set. Set spending limits to track category overruns.
                </p>
              )}
            </div>

            <div className="pt-3 mt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
              <span>{budgetOverview?.activeBudgetsCount || 0} active budgets</span>
              {budgetOverview?.overBudgetCount > 0 && (
                <span className="text-rose-600 font-medium flex items-center gap-1">
                  <AlertTriangle className="w-3 h-3" /> {budgetOverview.overBudgetCount} exceeded
                </span>
              )}
            </div>
          </Card>

          {/* Goals Widget */}
          <Card className="p-5 flex flex-col justify-between hover:shadow-md transition-shadow">
            <div>
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <div className="p-2 rounded-lg bg-emerald-50 text-emerald-600">
                    <Target className="w-4 h-4" />
                  </div>
                  <h3 className="text-sm font-bold text-slate-900">Savings Goals</h3>
                </div>
                <Link
                  to="/goals"
                  className="text-xs text-primary-600 hover:text-primary-700 font-medium inline-flex items-center gap-1"
                >
                  View Goals <ArrowRight className="w-3 h-3" />
                </Link>
              </div>

              {totalGoalTarget > 0 ? (
                <div className="space-y-2 mt-2">
                  <div className="flex justify-between text-xs">
                    <span className="text-slate-500">
                      Saved: <strong className="text-slate-900">{formatCurrency(totalGoalSaved, currency)}</strong>
                    </span>
                    <span className="text-slate-500">
                      Target: {formatCurrency(totalGoalTarget, currency)}
                    </span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                    <div
                      className="h-2 rounded-full bg-emerald-500 transition-all duration-500"
                      style={{ width: `${Math.min(goalProgress, 100)}%` }}
                    />
                  </div>
                  <div className="flex justify-between text-[11px] text-slate-400">
                    <span>{goalProgress.toFixed(1)}% reached</span>
                    <span className="text-emerald-600 font-medium">
                      {formatCurrency(Math.max(0, totalGoalTarget - totalGoalSaved), currency)} to go
                    </span>
                  </div>
                </div>
              ) : (
                <p className="text-xs text-slate-500 mt-2">
                  No active financial goals. Define targets to track savings milestones.
                </p>
              )}
            </div>

            <div className="pt-3 mt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
              <span>{goalSummary?.activeGoalsCount || 0} active goals</span>
              <span>{goalSummary?.completedGoalsCount || 0} completed</span>
            </div>
          </Card>

          {/* Upcoming Bills Widget */}
          <Card className="p-5 flex flex-col justify-between hover:shadow-md transition-shadow">
            <div>
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <div className="p-2 rounded-lg bg-amber-50 text-amber-600">
                    <Repeat className="w-4 h-4" />
                  </div>
                  <h3 className="text-sm font-bold text-slate-900">Upcoming Bills (14d)</h3>
                </div>
                <Link
                  to="/recurring"
                  className="text-xs text-primary-600 hover:text-primary-700 font-medium inline-flex items-center gap-1"
                >
                  All Schedules <ArrowRight className="w-3 h-3" />
                </Link>
              </div>

              {upcomingRecurring.length > 0 ? (
                <div className="space-y-2 mt-1">
                  {upcomingRecurring.slice(0, 3).map((item) => (
                    <div
                      key={item.recurringId}
                      className="flex items-center justify-between text-xs py-1 border-b border-slate-50 last:border-0"
                    >
                      <div>
                        <p className="font-semibold text-slate-800">{item.description}</p>
                        <p className="text-[10px] text-slate-400">
                          {item.dueDate} ({item.daysUntilDue === 0 ? 'Today' : `${item.daysUntilDue}d away`})
                        </p>
                      </div>
                      <span className="font-bold text-slate-900">
                        {formatCurrency(item.amount, currency)}
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-slate-500 mt-2">
                  No upcoming recurring bills scheduled in the next 14 days.
                </p>
              )}
            </div>

            <div className="pt-3 mt-3 border-t border-slate-100 text-xs text-slate-500">
              <span>{upcomingRecurring.length} payments due soon</span>
            </div>
          </Card>
        </div>
      )}

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
export default DashboardPage;
