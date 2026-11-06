import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { RecurringTransactionsPage } from '../../pages/recurring/RecurringTransactionsPage';
import { ToastProvider } from '../../context/ToastContext';
import { recurringService } from '../../services/recurringService';
import { categoryService } from '../../services/categoryService';
import * as authHook from '../../hooks/useAuth';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../services/recurringService', () => ({
  recurringService: {
    getRecurringTransactions: vi.fn(),
    getUpcomingRecurring: vi.fn(),
    createRecurringTransaction: vi.fn(),
    updateRecurringTransaction: vi.fn(),
    deleteRecurringTransaction: vi.fn(),
    pauseRecurringTransaction: vi.fn(),
    resumeRecurringTransaction: vi.fn(),
    cancelRecurringTransaction: vi.fn(),
    triggerProcessing: vi.fn(),
  },
}));

vi.mock('../../services/categoryService', () => ({
  categoryService: {
    getCategories: vi.fn(),
  },
}));

describe('RecurringTransactionsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authHook.useAuth.mockReturnValue({
      user: { fullName: 'Finance User', preferredCurrency: 'USD' },
    });
    categoryService.getCategories.mockResolvedValue([]);
    recurringService.getUpcomingRecurring.mockResolvedValue([]);
  });

  const renderRecurring = () => {
    return render(
      <MemoryRouter>
        <ToastProvider>
          <RecurringTransactionsPage />
        </ToastProvider>
      </MemoryRouter>
    );
  };

  it('renders recurring transactions table with active schedules', async () => {
    recurringService.getRecurringTransactions.mockResolvedValueOnce([
      {
        id: 'r-1',
        description: 'Netflix Subscription',
        merchant: 'Netflix Inc',
        category: { id: 'c-1', name: 'Entertainment', colorHex: '#8B5CF6' },
        amount: 15.99,
        type: 'EXPENSE',
        frequency: 'MONTHLY',
        intervalValue: 1,
        startDate: '2026-11-01',
        nextOccurrenceDate: '2026-12-01',
        status: 'ACTIVE',
      },
    ]);

    renderRecurring();

    await waitFor(() => {
      expect(screen.getByText('Recurring Transactions')).toBeInTheDocument();
      expect(screen.getByText('Netflix Subscription')).toBeInTheDocument();
    });

    expect(screen.getByText('Active Schedules')).toBeInTheDocument();
  });

  it('renders empty state when no recurring schedules exist', async () => {
    recurringService.getRecurringTransactions.mockResolvedValueOnce([]);

    renderRecurring();

    await waitFor(() => {
      expect(screen.getByText('No Recurring Transactions Found')).toBeInTheDocument();
    });
  });
});
