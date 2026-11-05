import React from 'react';
import { PiggyBank, Plus } from 'lucide-react';
import { Button } from '../../components/common/Button';
import { EmptyState } from '../../components/common/EmptyState';

export const BudgetsPage = () => {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Budget Management
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Set and track monthly category budget limits and overrun alerts.
          </p>
        </div>

        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />}>
          Create Budget
        </Button>
      </div>

      <EmptyState
        icon={PiggyBank}
        title="Budgeting & Overrun Tracking (Phase 4)"
        description="Category budget allocation, dynamic threshold notifications, and progress tracking will be implemented in Phase 4."
        className="py-16"
      />
    </div>
  );
};

