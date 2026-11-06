import React from 'react';
import { Edit2, Trash2 } from 'lucide-react';
import { Card } from '../common/Card';
import { Badge } from '../common/Badge';
import { formatCurrency } from '../../utils/currencyFormatter';
import { cn } from '../../utils/cn';

export const BudgetCard = ({
  budget,
  currency = 'USD',
  onEdit,
  onDelete,
}) => {
  const amount = parseFloat(budget.amount) || 0;
  const spent = parseFloat(budget.spentAmount) || 0;
  const remaining = parseFloat(budget.remainingAmount) || 0;
  const overAmount = parseFloat(budget.overBudgetAmount) || 0;
  const utilization = parseFloat(budget.utilizationPercentage) || 0;
  const isOverBudget = budget.overBudget || utilization >= 100;

  // Determine progress bar and badge color
  let progressColor = 'bg-emerald-500';
  let badgeVariant = 'success';
  let statusText = 'On Track';

  if (isOverBudget) {
    progressColor = 'bg-rose-600';
    badgeVariant = 'danger';
    statusText = `Exceeded by ${formatCurrency(overAmount > 0 ? overAmount : spent - amount, currency)}`;
  } else if (utilization >= (budget.alertThreshold || 80)) {
    progressColor = 'bg-amber-500';
    badgeVariant = 'warning';
    statusText = `${utilization.toFixed(0)}% Used (Near Limit)`;
  } else if (utilization >= 50) {
    progressColor = 'bg-indigo-500';
    badgeVariant = 'primary';
    statusText = `${utilization.toFixed(0)}% Used`;
  }

  const categoryName = budget.category ? budget.category.name : 'Overall (All Categories)';
  const categoryColor = budget.category?.colorHex || '#6366F1';

  return (
    <Card className="p-5 flex flex-col justify-between hover:shadow-md transition-shadow relative overflow-hidden">
      {/* Top Bar Accent */}
      <div
        className="absolute top-0 left-0 right-0 h-1"
        style={{ backgroundColor: categoryColor }}
      />

      <div>
        {/* Header */}
        <div className="flex items-start justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <span
                className="w-2.5 h-2.5 rounded-full inline-block"
                style={{ backgroundColor: categoryColor }}
              />
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                {categoryName}
              </span>
            </div>
            <h3 className="text-base font-bold text-slate-900 mt-1">
              {budget.name}
            </h3>
          </div>

          <Badge variant={badgeVariant} size="sm">
            {statusText}
          </Badge>
        </div>

        {/* Progress Bar */}
        <div className="mt-4">
          <div className="flex justify-between items-baseline text-xs mb-1.5">
            <span className="font-medium text-slate-600">
              Spent: <strong className="text-slate-900">{formatCurrency(spent, currency)}</strong>
            </span>
            <span className="text-slate-500">
              Budget: {formatCurrency(amount, currency)}
            </span>
          </div>

          <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
            <div
              className={cn('h-2.5 rounded-full transition-all duration-500', progressColor)}
              style={{ width: `${Math.min(utilization, 100)}%` }}
            />
          </div>

          <div className="flex justify-between items-center text-[11px] text-slate-400 mt-1.5">
            <span>Period: {budget.period?.toLowerCase()}</span>
            <span>
              {isOverBudget ? (
                <span className="text-rose-600 font-medium">Over limit</span>
              ) : (
                <span className="text-emerald-600 font-medium">
                  {formatCurrency(remaining, currency)} left
                </span>
              )}
            </span>
          </div>
        </div>
      </div>

      {/* Date Range and Actions Footer */}
      <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
        <span className="text-[11px] text-slate-400">
          {budget.startDate} to {budget.endDate}
        </span>

        <div className="flex items-center gap-1">
          <button
            type="button"
            onClick={() => onEdit(budget)}
            className="p-1 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded transition-colors"
            title="Edit Budget"
          >
            <Edit2 className="w-3.5 h-3.5" />
          </button>
          <button
            type="button"
            onClick={() => onDelete(budget)}
            className="p-1 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors"
            title="Delete Budget"
          >
            <Trash2 className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </Card>
  );
};
