import React from 'react';
import { Inbox } from 'lucide-react';
import { cn } from '../../utils/cn';

/**
 * Empty state visual component when data lists or charts have no items.
 */
export const EmptyState = ({
  icon: Icon = Inbox,
  title = 'No data available',
  description = 'There are no records to display at this time.',
  action,
  className,
}) => {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center p-8 text-center rounded-xl border border-dashed border-slate-200 bg-slate-50/50',
        className
      )}
    >
      <div className="p-3 bg-white rounded-full ring-1 ring-slate-200/70 shadow-sm text-slate-400 mb-3">
        <Icon className="w-6 h-6" aria-hidden="true" />
      </div>
      <h4 className="text-sm font-semibold text-slate-800">{title}</h4>
      <p className="mt-1 text-xs text-slate-500 max-w-sm">{description}</p>
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
};

