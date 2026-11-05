import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { DashboardPage } from '../../pages/dashboard/DashboardPage';
import { ToastProvider } from '../../context/ToastContext';
import { dashboardService } from '../../services/dashboardService';
import { categoryService } from '../../services/categoryService';
import * as authHook from '../../hooks/useAuth';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../services/dashboardService', () => ({
  dashboardService: {
    getDashboardSummary: vi.fn(),
    getSpendingTrend: vi.fn(),
    getCategoryBreakdown: vi.fn(),
    getRecentTransactions: vi.fn(),
  },
}));

vi.mock('../../services/categoryService', () => ({
  categoryService: {
    getCategories: vi.fn(),
  },
}));

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authHook.useAuth.mockReturnValue({
      user: { fullName: 'Alex Morgan', preferredCurrency: 'USD' },
    });
    categoryService.getCategories.mockResolvedValue([]);
    dashboardService.getSpendingTrend.mockResolvedValue([
      { period: 'Nov', income: 4500, expenses: 2200 },
    ]);
    dashboardService.getCategoryBreakdown.mockResolvedValue([
      { categoryName: 'Groceries', amount: 500 },
    ]);
    dashboardService.getRecentTransactions.mockResolvedValue([]);
  });

  const renderDashboard = () => {
    return render(
      <MemoryRouter>
        <ToastProvider>
          <DashboardPage />
        </ToastProvider>
      </MemoryRouter>
    );
  };

  it('renders live KPI metrics from dashboard summary API', async () => {
    dashboardService.getDashboardSummary.mockResolvedValueOnce({
      totalIncome: 6000.0,
      totalExpense: 2400.0,
      netBalance: 3600.0,
      savingsRatePercent: 60.0,
      transactionCount: 8,
      currency: 'USD',
    });

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText('Welcome back, Alex Morgan!')).toBeInTheDocument();
      expect(screen.getByText('$6,000.00')).toBeInTheDocument();
      expect(screen.getByText('$2,400.00')).toBeInTheDocument();
      expect(screen.getByText('$3,600.00')).toBeInTheDocument();
      expect(screen.getByText('60.0%')).toBeInTheDocument();
    });
  });

  it('renders empty state when transaction count is zero', async () => {
    dashboardService.getDashboardSummary.mockResolvedValueOnce({
      totalIncome: 0,
      totalExpense: 0,
      netBalance: 0,
      savingsRatePercent: 0,
      transactionCount: 0,
      currency: 'USD',
    });

    renderDashboard();

    await waitFor(() => {
      expect(screen.getByText('No transactions recorded yet')).toBeInTheDocument();
    });
  });
});
