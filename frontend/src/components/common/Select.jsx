import React from 'react';
import { AlertCircle } from 'lucide-react';
import { cn } from '../../utils/cn';

/**
 * Reusable Select dropdown component.
 */
export const Select = React.forwardRef(
  (
    {
      label,
      id,
      name,
      options = [],
      error,
      helperText,
      className,
      containerClassName,
      required = false,
      disabled = false,
      placeholder,
      children,
      ...props
    },
    ref
  ) => {
    const selectId = id || name;
    const hasError = Boolean(error);
    const errorId = selectId ? `${selectId}-error` : undefined;
    const helperId = selectId ? `${selectId}-helper` : undefined;

    return (
      <div className={cn('w-full', containerClassName)}>
        {label && (
          <label
            htmlFor={selectId}
            className="block text-sm font-medium text-slate-700 mb-1"
          >
            {label}
            {required && <span className="text-rose-500 ml-1" aria-hidden="true">*</span>}
          </label>
        )}

        <div className="relative rounded-lg shadow-sm">
          <select
            ref={ref}
            id={selectId}
            name={name}
            disabled={disabled}
            required={required}
            aria-invalid={hasError ? 'true' : 'false'}
            aria-describedby={
              hasError ? errorId : helperText ? helperId : undefined
            }
            className={cn(
              'block w-full rounded-lg border text-sm transition-colors py-2.5 px-3.5 bg-white',
              'focus:outline-none focus:ring-2 focus:ring-offset-0',
              hasError
                ? 'border-rose-300 text-rose-900 focus:border-rose-500 focus:ring-rose-200'
                : 'border-slate-300 text-slate-900 focus:border-primary-500 focus:ring-primary-100',
              disabled && 'bg-slate-50 text-slate-400 cursor-not-allowed border-slate-200',
              className
            )}
            {...props}
          >
            {placeholder && (
              <option value="" disabled>
                {placeholder}
              </option>
            )}
            {options.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
            {children}
          </select>
        </div>

        {hasError && (
          <div
            id={errorId}
            role="alert"
            className="mt-1.5 flex items-center gap-1.5 text-xs text-rose-600 animate-fadeIn"
          >
            <AlertCircle className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
            <span>{error}</span>
          </div>
        )}

        {!hasError && helperText && (
          <p id={helperId} className="mt-1.5 text-xs text-slate-500">
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Select.displayName = 'Select';

