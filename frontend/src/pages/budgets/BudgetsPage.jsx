import React, { useState, useEffect, useCallback } from 'react';
import {
  PiggyBank,
  Plus,
  AlertTriangle,
  TrendingDown,
  DollarSign,
  Bell,
  RefreshCw,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { Button } from '../../components/common/Button';
import { StatCard } from '../../components/common/StatCard';
import { EmptyState } from '../../components/common/EmptyState';
import { CardSkeleton } from '../../components/common/SkeletonLoader';
import { BudgetCard } from '../../components/budgets/BudgetCard';
import { AddEditBudgetModal } from '../../components/budgets/AddEditBudgetModal';
import { DeleteConfirmModal } from '../../components/transactions/DeleteConfirmModal';
import { budgetService } from '../../services/budgetService';
import { categoryService } from '../../services/categoryService';
import { formatCurrency } from '../../utils/currencyFormatter';

export const BudgetsPage = () => {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const currency = user?.preferredCurrency || 'USD';

  const [isLoading, setIsLoading] = useState(true);
  const [budgets, setBudgets] = useState([]);
  const [overview, setOverview] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [categories, setCategories] = useState([]);

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedBudgetForEdit, setSelectedBudgetForEdit] = useState(null);
  const [selectedBudgetForDelete, setSelectedBudgetForDelete] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchBudgetsData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [budgetsRes, overviewRes, alertsRes, catsRes] = await Promise.all([
        budgetService.getBudgets(),
        budgetService.getBudgetOverview().catch(() => null),
        budgetService.getAlerts(true).catch(() => []),
        categoryService.getCategories().catch(() => []),
      ]);

      const budgetList = budgetsRes?.data || budgetsRes || [];
      setBudgets(Array.isArray(budgetList) ? budgetList : []);

      setOverview(overviewRes?.data || overviewRes || null);

      const alertList = alertsRes?.data || alertsRes || [];
      setAlerts(Array.isArray(alertList) ? alertList : []);

      const catList = catsRes?.data || catsRes || [];
      setCategories(Array.isArray(catList) ? catList : []);
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to load budgets';
      toastError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [toastError]);

  useEffect(() => {
    fetchBudgetsData();
  }, [fetchBudgetsData]);

  const handleOpenCreateModal = () => {
    setSelectedBudgetForEdit(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (budget) => {
    setSelectedBudgetForEdit(budget);
    setIsModalOpen(true);
  };

  const handleSaveBudget = async (budgetData, id) => {
    try {
      if (id) {
        await budgetService.updateBudget(id, budgetData);
        success('Budget updated successfully');
      } else {
        await budgetService.createBudget(budgetData);
        success('Budget created successfully');
      }
      fetchBudgetsData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to save budget';
      toastError(msg);
      throw err;
    }
  };

  const handleDeleteBudget = async () => {
    if (!selectedBudgetForDelete) return;
    setIsDeleting(true);
    try {
      await budgetService.deleteBudget(selectedBudgetForDelete.id);
      success('Budget deleted successfully');
      setSelectedBudgetForDelete(null);
      fetchBudgetsData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to delete budget';
      toastError(msg);
    } finally {
      setIsDeleting(false);
    }
  };

  const totalBudgeted = parseFloat(overview?.totalBudgeted) || 0;
  const totalSpent = parseFloat(overview?.totalSpent) || 0;
  const totalRemaining = parseFloat(overview?.totalRemaining) || 0;
  const overallUtilization = parseFloat(overview?.overallUtilizationPercentage) || 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Budget Management
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Set and track spending limits, threshold alerts, and category utilization.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchBudgetsData}
            leftIcon={<RefreshCw className="w-4 h-4" />}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreateModal}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Create Budget
          </Button>
        </div>
      </div>

      {/* Unread Alerts Banner */}
      {alerts.length > 0 && (
        <div className="p-4 rounded-xl bg-amber-50 border border-amber-200 flex items-start gap-3">
          <Bell className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
          <div className="flex-1">
            <h4 className="text-sm font-semibold text-amber-900">
              Budget Spending Alerts ({alerts.length})
            </h4>
            <div className="mt-1 space-y-1">
              {alerts.slice(0, 3).map((alert) => (
                <p key={alert.id} className="text-xs text-amber-800">
                  {alert.message}
                </p>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* KPI Overview Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Budgeted"
          value={formatCurrency(totalBudgeted, currency)}
          subtitle="Active monthly/weekly limits"
          icon={PiggyBank}
          iconColor="text-indigo-600"
          iconBg="bg-indigo-50"
        />
        <StatCard
          title="Total Spent"
          value={formatCurrency(totalSpent, currency)}
          subtitle={`${overallUtilization.toFixed(1)}% of total budget`}
          icon={TrendingDown}
          iconColor="text-rose-600"
          iconBg="bg-rose-50"
        />
        <StatCard
          title="Remaining Budget"
          value={formatCurrency(totalRemaining, currency)}
          subtitle={totalRemaining >= 0 ? 'Safe margin' : 'Deficit exceeded'}
          icon={DollarSign}
          iconColor={totalRemaining >= 0 ? 'text-emerald-600' : 'text-rose-600'}
          iconBg={totalRemaining >= 0 ? 'bg-emerald-50' : 'bg-rose-50'}
        />
        <StatCard
          title="Active Budgets"
          value={budgets.length}
          subtitle={`${overview?.overBudgetCount || 0} exceeded limits`}
          icon={AlertTriangle}
          iconColor={overview?.overBudgetCount > 0 ? 'text-rose-600' : 'text-slate-600'}
          iconBg={overview?.overBudgetCount > 0 ? 'bg-rose-50' : 'bg-slate-50'}
        />
      </div>

      {/* Budgets Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          <CardSkeleton />
          <CardSkeleton />
          <CardSkeleton />
        </div>
      ) : budgets.length === 0 ? (
        <EmptyState
          icon={PiggyBank}
          title="No Budgets Created Yet"
          description="Create your first category or overall budget to monitor spending limits and avoid financial surprises."
          action={
            <Button
              variant="primary"
              size="sm"
              onClick={handleOpenCreateModal}
              leftIcon={<Plus className="w-4 h-4" />}
            >
              Create Your First Budget
            </Button>
          }
          className="py-16"
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {budgets.map((budget) => (
            <BudgetCard
              key={budget.id}
              budget={budget}
              currency={currency}
              onEdit={handleOpenEditModal}
              onDelete={(b) => setSelectedBudgetForDelete(b)}
            />
          ))}
        </div>
      )}

      {/* Add / Edit Modal */}
      <AddEditBudgetModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSaveBudget}
        budget={selectedBudgetForEdit}
        categories={categories}
      />

      {/* Delete Confirmation Modal */}
      <DeleteConfirmModal
        isOpen={!!selectedBudgetForDelete}
        onClose={() => setSelectedBudgetForDelete(null)}
        onConfirm={handleDeleteBudget}
        title="Delete Budget"
        message={`Are you sure you want to delete budget "${selectedBudgetForDelete?.name}"? Tracked expenses will not be deleted, but limits and threshold alerts will be removed.`}
        isDeleting={isDeleting}
      />
    </div>
  );
};
export default BudgetsPage;
