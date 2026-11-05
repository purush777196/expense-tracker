import React from 'react';
import { cn } from '../../utils/cn';

/**
 * Reusable Card container component.
 */
export const Card = ({ children, className, ...props }) => {
  return (
    <div
      className={cn(
        'bg-white rounded-xl border border-slate-200 shadow-sm transition-shadow',
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export const CardHeader = ({ title, subtitle, action, className, children }) => {
  return (
    <div
      className={cn(
        'px-6 py-5 border-b border-slate-100 flex items-center justify-between gap-4',
        className
      )}
    >
      <div>
        {title && <h3 className="text-base font-semibold text-slate-900">{title}</h3>}
        {subtitle && <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>}
        {children}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  );
};

export const CardContent = ({ children, className }) => {
  return <div className={cn('p-6', className)}>{children}</div>;
};

export const CardFooter = ({ children, className }) => {
  return (
    <div
      className={cn(
        'px-6 py-4 bg-slate-50/60 border-t border-slate-100 rounded-b-xl flex items-center justify-between',
        className
      )}
    >
      {children}
    </div>
  );
};

