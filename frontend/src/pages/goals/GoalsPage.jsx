import React, { useState, useEffect, useCallback } from 'react';
import {
  Target,
  Plus,
  TrendingUp,
  CheckCircle2,
  DollarSign,
  RefreshCw,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';
import { Button } from '../../components/common/Button';
import { StatCard } from '../../components/common/StatCard';
import { EmptyState } from '../../components/common/EmptyState';
import { CardSkeleton } from '../../components/common/SkeletonLoader';
import { GoalCard } from '../../components/goals/GoalCard';
import { AddEditGoalModal } from '../../components/goals/AddEditGoalModal';
import { GoalContributionModal } from '../../components/goals/GoalContributionModal';
import { ContributionHistoryModal } from '../../components/goals/ContributionHistoryModal';
import { DeleteConfirmModal } from '../../components/transactions/DeleteConfirmModal';
import { goalService } from '../../services/goalService';
import { formatCurrency } from '../../utils/currencyFormatter';

export const GoalsPage = () => {
  const { user } = useAuth();
  const { success, error: toastError } = useToast();
  const currency = user?.preferredCurrency || 'USD';

  const [isLoading, setIsLoading] = useState(true);
  const [goals, setGoals] = useState([]);
  const [summary, setSummary] = useState(null);

  // Modal states
  const [isAddEditModalOpen, setIsAddEditModalOpen] = useState(false);
  const [selectedGoalForEdit, setSelectedGoalForEdit] = useState(null);
  const [selectedGoalForContribute, setSelectedGoalForContribute] = useState(null);
  const [selectedGoalForHistory, setSelectedGoalForHistory] = useState(null);
  const [selectedGoalForDelete, setSelectedGoalForDelete] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchGoalsData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [goalsRes, summaryRes] = await Promise.all([
        goalService.getGoals(),
        goalService.getGoalSummary().catch(() => null),
      ]);

      const goalList = goalsRes?.data || goalsRes || [];
      setGoals(Array.isArray(goalList) ? goalList : []);
      setSummary(summaryRes?.data || summaryRes || null);
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to load financial goals';
      toastError(msg);
    } finally {
      setIsLoading(false);
    }
  }, [toastError]);

  useEffect(() => {
    fetchGoalsData();
  }, [fetchGoalsData]);

  const handleOpenCreateModal = () => {
    setSelectedGoalForEdit(null);
    setIsAddEditModalOpen(true);
  };

  const handleOpenEditModal = (goal) => {
    setSelectedGoalForEdit(goal);
    setIsAddEditModalOpen(true);
  };

  const handleSaveGoal = async (goalData, id) => {
    try {
      if (id) {
        await goalService.updateGoal(id, goalData);
        success('Goal updated successfully');
      } else {
        await goalService.createGoal(goalData);
        success('Financial goal created successfully');
      }
      fetchGoalsData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to save goal';
      toastError(msg);
      throw err;
    }
  };

  const handleRecordContribution = async (goalId, data) => {
    try {
      await goalService.recordContribution(goalId, data);
      success(`${data.type === 'DEPOSIT' ? 'Deposit' : 'Withdrawal'} recorded successfully`);
      fetchGoalsData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to record contribution';
      toastError(msg);
      throw err;
    }
  };

  const handleDeleteGoal = async () => {
    if (!selectedGoalForDelete) return;
    setIsDeleting(true);
    try {
      await goalService.deleteGoal(selectedGoalForDelete.id);
      success('Financial goal deleted successfully');
      setSelectedGoalForDelete(null);
      fetchGoalsData();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Failed to delete goal';
      toastError(msg);
    } finally {
      setIsDeleting(false);
    }
  };

  const totalTarget = parseFloat(summary?.totalTargetAmount) || 0;
  const totalSaved = parseFloat(summary?.totalSavedAmount) || 0;
  const overallProgress = parseFloat(summary?.overallProgressPercentage) || 0;
  const activeCount = summary?.activeGoalsCount || goals.filter((g) => g.status === 'IN_PROGRESS').length;
  const completedCount = summary?.completedGoalsCount || goals.filter((g) => g.status === 'COMPLETED').length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Financial Goals
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Track emergency reserves, milestone savings targets, and contribution histories.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchGoalsData}
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
            New Goal
          </Button>
        </div>
      </div>

      {/* KPI Overview Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Target Amount"
          value={formatCurrency(totalTarget, currency)}
          subtitle="Cumulative milestone targets"
          icon={Target}
          iconColor="text-indigo-600"
          iconBg="bg-indigo-50"
        />
        <StatCard
          title="Total Saved Amount"
          value={formatCurrency(totalSaved, currency)}
          subtitle={`${overallProgress.toFixed(1)}% of targets reached`}
          icon={DollarSign}
          iconColor="text-emerald-600"
          iconBg="bg-emerald-50"
        />
        <StatCard
          title="Active Goals"
          value={activeCount}
          subtitle="Goals in active progress"
          icon={TrendingUp}
          iconColor="text-primary-600"
          iconBg="bg-primary-50"
        />
        <StatCard
          title="Completed Goals"
          value={completedCount}
          subtitle="Targets achieved"
          icon={CheckCircle2}
          iconColor="text-emerald-600"
          iconBg="bg-emerald-50"
        />
      </div>

      {/* Goals Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          <CardSkeleton />
          <CardSkeleton />
          <CardSkeleton />
        </div>
      ) : goals.length === 0 ? (
        <EmptyState
          icon={Target}
          title="No Financial Goals Set"
          description="Create savings targets for an emergency fund, home down-payment, vacation, or retirement cushion."
          action={
            <Button
              variant="primary"
              size="sm"
              onClick={handleOpenCreateModal}
              leftIcon={<Plus className="w-4 h-4" />}
            >
              Set Your First Goal
            </Button>
          }
          className="py-16"
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {goals.map((goal) => (
            <GoalCard
              key={goal.id}
              goal={goal}
              currency={currency}
              onEdit={handleOpenEditModal}
              onDelete={(g) => setSelectedGoalForDelete(g)}
              onContribute={(g) => setSelectedGoalForContribute(g)}
              onViewHistory={(g) => setSelectedGoalForHistory(g)}
            />
          ))}
        </div>
      )}

      {/* Add / Edit Goal Modal */}
      <AddEditGoalModal
        isOpen={isAddEditModalOpen}
        onClose={() => setIsAddEditModalOpen(false)}
        onSave={handleSaveGoal}
        goal={selectedGoalForEdit}
      />

      {/* Contribution Modal */}
      <GoalContributionModal
        isOpen={!!selectedGoalForContribute}
        onClose={() => setSelectedGoalForContribute(null)}
        goal={selectedGoalForContribute}
        onRecord={handleRecordContribution}
        currency={currency}
      />

      {/* Contribution History Modal */}
      <ContributionHistoryModal
        isOpen={!!selectedGoalForHistory}
        onClose={() => setSelectedGoalForHistory(null)}
        goal={selectedGoalForHistory}
        currency={currency}
      />

      {/* Delete Confirmation Modal */}
      <DeleteConfirmModal
        isOpen={!!selectedGoalForDelete}
        onClose={() => setSelectedGoalForDelete(null)}
        onConfirm={handleDeleteGoal}
        title="Delete Financial Goal"
        message={`Are you sure you want to delete goal "${selectedGoalForDelete?.title}"? All associated contribution records will be permanently removed.`}
        isDeleting={isDeleting}
      />
    </div>
  );
};
export default GoalsPage;
