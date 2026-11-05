import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { AddEditTransactionModal } from '../../components/transactions/AddEditTransactionModal';
import { ToastProvider } from '../../context/ToastContext';
import { transactionService } from '../../services/transactionService';

vi.mock('../../services/transactionService', () => ({
  transactionService: {
    createTransaction: vi.fn(),
    updateTransaction: vi.fn(),
  },
}));

const mockCategories = [
  { id: 'cat-1', name: 'Groceries', type: 'EXPENSE' },
  { id: 'cat-2', name: 'Utilities', type: 'EXPENSE' },
  { id: 'cat-3', name: 'Salary', type: 'INCOME' },
];

describe('AddEditTransactionModal', () => {
  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderModal = (props = {}) => {
    return render(
      <ToastProvider>
        <AddEditTransactionModal
          isOpen={true}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
          categories={mockCategories}
          currency="USD"
          {...props}
        />
      </ToastProvider>
    );
  };

  it('renders modal with required inputs and filtered expense categories by default', () => {
    renderModal();

    expect(screen.getByRole('heading', { name: /record transaction/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/amount/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();

    // Verify only expense categories are listed initially
    expect(screen.getByRole('option', { name: 'Groceries' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Utilities' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Salary' })).not.toBeInTheDocument();
  });

  it('switches category options when Income type tab is selected', () => {
    renderModal();

    fireEvent.click(screen.getByRole('button', { name: /income/i }));

    // Now Salary should be available and Groceries should be removed
    expect(screen.getByRole('option', { name: 'Salary' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Groceries' })).not.toBeInTheDocument();
  });

  it('validates empty inputs on submit', async () => {
    renderModal();

    fireEvent.click(screen.getByRole('button', { name: /record transaction/i }));

    await waitFor(() => {
      expect(screen.getByText(/please enter a valid amount/i)).toBeInTheDocument();
      expect(screen.getByText(/description is required/i)).toBeInTheDocument();
    });

    expect(transactionService.createTransaction).not.toHaveBeenCalled();
  });

  it('submits valid transaction data and calls onSuccess', async () => {
    transactionService.createTransaction.mockResolvedValueOnce({
      id: 'tx-101',
      amount: 45.0,
      description: 'Dinner with team',
    });

    renderModal();

    fireEvent.change(screen.getByLabelText(/amount/i), { target: { value: '45.00' } });
    fireEvent.change(screen.getByLabelText(/description/i), { target: { value: 'Dinner with team' } });

    fireEvent.click(screen.getByRole('button', { name: /record transaction/i }));

    await waitFor(() => {
      expect(transactionService.createTransaction).toHaveBeenCalledWith(
        expect.objectContaining({
          amount: 45.0,
          type: 'EXPENSE',
          description: 'Dinner with team',
          categoryId: 'cat-1',
        })
      );
      expect(mockOnSuccess).toHaveBeenCalled();
      expect(mockOnClose).toHaveBeenCalled();
    });
  });
});
