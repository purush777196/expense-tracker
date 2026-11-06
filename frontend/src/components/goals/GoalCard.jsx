import React from 'react';
import { Target, PlusCircle, History, Edit2, Trash2, CheckCircle2 } from 'lucide-react';
import { Card } from '../common/Card';
import { Badge } from '../common/Badge';
import { Button } from '../common/Button';
import { formatCurrency } from '../../utils/currencyFormatter';
import { cn } from '../../utils/cn';

export const GoalCard = ({
  goal,
  currency = 'USD',
  onEdit,
  onDelete,
  onContribute,
  onViewHistory,
}) => {
  const current = parseFloat(goal.currentAmount) || 0;
  const target = parseFloat(goal.targetAmount) || 0;
  const progress = parseFloat(goal.progressPercentage) || 0;
  const isCompleted = goal.status === 'COMPLETED' || progress >= 100;

  let badgeVariant = 'primary';
  if (isCompleted) badgeVariant = 'success';
  else if (goal.status === 'CANCELLED') badgeVariant = 'neutral';

  return (
    <Card className="p-5 flex flex-col justify-between hover:shadow-md transition-shadow">
      <div>
        {/* Top Header */}
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3">
            <div className="p-2.5 rounded-xl bg-indigo-50 text-indigo-600 shrink-0">
              <Target className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900 leading-tight">
                {goal.title}
              </h3>
              {goal.description && (
                <p className="text-xs text-slate-500 mt-0.5 line-clamp-1">
                  {goal.description}
                </p>
              )}
            </div>
          </div>

          <Badge variant={badgeVariant} size="sm">
            {isCompleted ? 'Completed' : `${progress.toFixed(0)}%`}
          </Badge>
        </div>

        {/* Progress Numbers */}
        <div className="mt-5 space-y-2">
          <div className="flex justify-between items-baseline text-xs">
            <span className="text-slate-500">
              Saved: <strong className="text-slate-900 font-bold">{formatCurrency(current, currency)}</strong>
            </span>
            <span className="text-slate-500">
              Target: {formatCurrency(target, currency)}
            </span>
          </div>

          {/* Progress bar */}
          <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
            <div
              className={cn(
                'h-2.5 rounded-full transition-all duration-500',
                isCompleted ? 'bg-emerald-500' : 'bg-indigo-600'
              )}
              style={{ width: `${Math.min(progress, 100)}%` }}
            />
          </div>

          <div className="flex justify-between items-center text-[11px] text-slate-400">
            <span>Target date: {goal.targetDate}</span>
            <span>
              {isCompleted ? (
                <span className="text-emerald-600 font-medium flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3" /> Reached
                </span>
              ) : (
                <span>{goal.daysRemaining != null ? `${goal.daysRemaining} days remaining` : ''}</span>
              )}
            </span>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => onContribute(goal)}
            leftIcon={<PlusCircle className="w-3.5 h-3.5" />}
          >
            Contribute
          </Button>

          <button
            type="button"
            onClick={() => onViewHistory(goal)}
            className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
            title="View History"
          >
            <History className="w-4 h-4" />
          </button>
        </div>

        <div className="flex items-center gap-1">
          <button
            type="button"
            onClick={() => onEdit(goal)}
            className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
            title="Edit Goal"
          >
            <Edit2 className="w-3.5 h-3.5" />
          </button>
          <button
            type="button"
            onClick={() => onDelete(goal)}
            className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
            title="Delete Goal"
          >
            <Trash2 className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </Card>
  );
};
