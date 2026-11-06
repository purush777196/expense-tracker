import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { BudgetsPage } from '../../pages/budgets/BudgetsPage';
import { ToastProvider } from '../../context/ToastContext';
import { budgetService } from '../../services/budgetService';
import { categoryService } from '../../services/categoryService';
import * as authHook from '../../hooks/useAuth';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../services/budgetService', () => ({
  budgetService: {
    getBudgets: vi.fn(),
    getBudgetOverview: vi.fn(),
    getAlerts: vi.fn(),
    createBudget: vi.fn(),
    updateBudget: vi.fn(),
    deleteBudget: vi.fn(),
  },
}));

vi.mock('../../services/categoryService', () => ({
  categoryService: {
    getCategories: vi.fn(),
  },
}));

describe('BudgetsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authHook.useAuth.mockReturnValue({
      user: { fullName: 'Finance User', preferredCurrency: 'USD' },
    });
    categoryService.getCategories.mockResolvedValue([]);
    budgetService.getAlerts.mockResolvedValue([]);
  });

  const renderBudgets = () => {
    return render(
      <MemoryRouter>
        <ToastProvider>
          <BudgetsPage />
        </ToastProvider>
      </MemoryRouter>
    );
  };

  it('renders budget management KPI overview and active budgets', async () => {
    budgetService.getBudgetOverview.mockResolvedValueOnce({
      totalBudgeted: 2000.0,
      totalSpent: 950.0,
      totalRemaining: 1050.0,
      overallUtilizationPercentage: 47.5,
      activeBudgetsCount: 1,
      overBudgetCount: 0,
    });

    budgetService.getBudgets.mockResolvedValueOnce([
      {
        id: 'b-1',
        name: 'Dining & Food',
        category: { id: 'c-1', name: 'Food', colorHex: '#EF4444' },
        amount: 500.0,
        spentAmount: 250.0,
        remainingAmount: 250.0,
        overBudgetAmount: 0.0,
        utilizationPercentage: 50.0,
        period: 'MONTHLY',
        startDate: '2026-11-01',
        endDate: '2026-11-30',
        alertThreshold: 80,
      },
    ]);

    renderBudgets();

    await waitFor(() => {
      expect(screen.getByText('Budget Management')).toBeInTheDocument();
      expect(screen.getByText('Dining & Food')).toBeInTheDocument();
    });

    expect(screen.getByText('Total Budgeted')).toBeInTheDocument();
    expect(screen.getByText('Remaining Budget')).toBeInTheDocument();
  });

  it('renders empty state when no budgets exist', async () => {
    budgetService.getBudgetOverview.mockResolvedValueOnce({
      totalBudgeted: 0,
      totalSpent: 0,
      totalRemaining: 0,
      overallUtilizationPercentage: 0,
      activeBudgetsCount: 0,
      overBudgetCount: 0,
    });
    budgetService.getBudgets.mockResolvedValueOnce([]);

    renderBudgets();

    await waitFor(() => {
      expect(screen.getByText('No Budgets Created Yet')).toBeInTheDocument();
    });
  });
});
