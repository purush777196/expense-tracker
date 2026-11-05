import React from 'react';
import { Target, Plus } from 'lucide-react';
import { Button } from '../../components/common/Button';
import { EmptyState } from '../../components/common/EmptyState';

export const GoalsPage = () => {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900">
            Financial Goals
          </h2>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Track emergency funds, debt payoffs, and milestone savings targets.
          </p>
        </div>

        <Button variant="primary" size="sm" leftIcon={<Plus className="w-4 h-4" />}>
          New Goal
        </Button>
      </div>

      <EmptyState
        icon={Target}
        title="Savings Goals & Milestones"
        description="Target deadline tracking, milestone auto-allocations, and goal completion progress bars will be available in future releases."
        className="py-16"
      />
    </div>
  );
};

