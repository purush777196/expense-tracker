import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Badge } from '../../components/common/Badge';

describe('Badge Component', () => {
  it('renders badge content', () => {
    render(<Badge>Active</Badge>);
    expect(screen.getByText('Active')).toBeInTheDocument();
  });

  it('applies variant classes for income and expense', () => {
    const { rerender } = render(<Badge variant="income">Income</Badge>);
    expect(screen.getByText('Income')).toHaveClass('bg-emerald-50');

    rerender(<Badge variant="expense">Expense</Badge>);
    expect(screen.getByText('Expense')).toHaveClass('bg-rose-50');
  });
});

