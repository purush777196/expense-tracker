import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { TransactionsPage } from '../../pages/transactions/TransactionsPage';
import { ToastProvider } from '../../context/ToastContext';
import { transactionService } from '../../services/transactionService';
import { categoryService } from '../../services/categoryService';
import * as authHook from '../../hooks/useAuth';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../services/transactionService', () => ({
  transactionService: {
    getTransactions: vi.fn(),
    deleteTransaction: vi.fn(),
    createTransaction: vi.fn(),
    updateTransaction: vi.fn(),
  },
}));

vi.mock('../../services/categoryService', () => ({
  categoryService: {
    getCategories: vi.fn(),
  },
}));

const mockTransactions = [
  {
    id: 'tx-1',
    description: 'Tech Payroll',
    amount: 5000.0,
    type: 'INCOME',
    transactionDate: '2026-11-01T10:00:00Z',
    category: { id: 'c1', name: 'Salary', type: 'INCOME' },
    paymentMethod: 'BANK_TRANSFER',
  },
  {
    id: 'tx-2',
    description: 'Whole Foods Groceries',
    amount: 120.5,
    type: 'EXPENSE',
    transactionDate: '2026-11-02T15:30:00Z',
    category: { id: 'c2', name: 'Groceries', type: 'EXPENSE' },
    paymentMethod: 'CARD',
  },
];

describe('TransactionsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authHook.useAuth.mockReturnValue({
      user: { fullName: 'Jane Doe', preferredCurrency: 'USD' },
    });
    categoryService.getCategories.mockResolvedValue([
      { id: 'c1', name: 'Salary', type: 'INCOME' },
      { id: 'c2', name: 'Groceries', type: 'EXPENSE' },
    ]);
  });

  const renderPage = () => {
    return render(
      <MemoryRouter>
        <ToastProvider>
          <TransactionsPage />
        </ToastProvider>
      </MemoryRouter>
    );
  };

  it('renders transactions table with fetched data', async () => {
    transactionService.getTransactions.mockResolvedValueOnce({
      content: mockTransactions,
      totalElements: 2,
      totalPages: 1,
      pageNumber: 0,
      pageSize: 10,
      isLast: true,
    });

    renderPage();

    await waitFor(() => {
      expect(screen.getByText('Tech Payroll')).toBeInTheDocument();
      expect(screen.getByText('Whole Foods Groceries')).toBeInTheDocument();
      expect(screen.getByText('+$5,000.00')).toBeInTheDocument();
      expect(screen.getByText('-$120.50')).toBeInTheDocument();
    });
  });

  it('filters by transaction type when expense pill is clicked', async () => {
    transactionService.getTransactions.mockResolvedValue({
      content: [mockTransactions[1]],
      totalElements: 1,
      totalPages: 1,
      pageNumber: 0,
      pageSize: 10,
      isLast: true,
    });

    renderPage();

    fireEvent.click(screen.getByRole('button', { name: /^expenses$/i }));

    await waitFor(() => {
      expect(transactionService.getTransactions).toHaveBeenCalledWith(
        expect.objectContaining({ type: 'EXPENSE' })
      );
    });
  });

  it('opens Add Transaction modal when button clicked', async () => {
    transactionService.getTransactions.mockResolvedValueOnce({
      content: [],
      totalElements: 0,
      totalPages: 1,
      pageNumber: 0,
      pageSize: 10,
      isLast: true,
    });

    renderPage();

    const addButtons = screen.getAllByRole('button', { name: /add.*transaction/i });
    fireEvent.click(addButtons[0]);

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /record transaction/i })).toBeInTheDocument();
    });
  });
});
