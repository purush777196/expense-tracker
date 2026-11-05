import React from 'react';
import { TrendingUp, TrendingDown, Minus } from 'lucide-react';
import { Card } from './Card';
import { cn } from '../../utils/cn';

/**
 * StatCard component for KPI metrics on the dashboard (Balance, Income, Expense, Savings Rate).
 */
export const StatCard = ({
  title,
  value,
  subtitle,
  icon: Icon,
  iconColor = 'text-primary-600',
  iconBg = 'bg-primary-50',
  trend, // { direction: 'up' | 'down' | 'neutral', label: string }
  trendInverted = false, // If true, up is negative (like expenses increasing)
  className,
}) => {
  let trendColor = 'text-slate-500';
  let TrendIcon = Minus;

  if (trend) {
    if (trend.direction === 'up') {
      TrendIcon = TrendingUp;
      trendColor = trendInverted ? 'text-rose-600' : 'text-emerald-600';
    } else if (trend.direction === 'down') {
      TrendIcon = TrendingDown;
      trendColor = trendInverted ? 'text-emerald-600' : 'text-rose-600';
    }
  }

  return (
    <Card className={cn('p-6 hover:shadow-md transition-all', className)}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium uppercase tracking-wider text-slate-500">
            {title}
          </p>
          <div className="mt-2 flex items-baseline">
            <span className="text-2xl font-bold tracking-tight text-slate-900">
              {value}
            </span>
          </div>
        </div>
        {Icon && (
          <div className={cn('p-3 rounded-xl ring-1 ring-black/5', iconBg)}>
            <Icon className={cn('w-5 h-5', iconColor)} aria-hidden="true" />
          </div>
        )}
      </div>

      {(trend || subtitle) && (
        <div className="mt-4 flex items-center gap-2 text-xs">
          {trend && (
            <span className={cn('inline-flex items-center font-medium gap-1', trendColor)}>
              <TrendIcon className="w-3.5 h-3.5" />
              {trend.label}
            </span>
          )}
          {subtitle && (
            <span className="text-slate-500 truncate">{subtitle}</span>
          )}
        </div>
      )}
    </Card>
  );
};

