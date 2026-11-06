import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { GoalsPage } from '../../pages/goals/GoalsPage';
import { ToastProvider } from '../../context/ToastContext';
import { goalService } from '../../services/goalService';
import * as authHook from '../../hooks/useAuth';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../../services/goalService', () => ({
  goalService: {
    getGoals: vi.fn(),
    getGoalSummary: vi.fn(),
    createGoal: vi.fn(),
    updateGoal: vi.fn(),
    deleteGoal: vi.fn(),
    recordContribution: vi.fn(),
    getContributions: vi.fn(),
  },
}));

describe('GoalsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authHook.useAuth.mockReturnValue({
      user: { fullName: 'Finance User', preferredCurrency: 'USD' },
    });
  });

  const renderGoals = () => {
    return render(
      <MemoryRouter>
        <ToastProvider>
          <GoalsPage />
        </ToastProvider>
      </MemoryRouter>
    );
  };

  it('renders financial goals overview and active goals', async () => {
    goalService.getGoalSummary.mockResolvedValueOnce({
      totalTargetAmount: 10000.0,
      totalSavedAmount: 4000.0,
      overallProgressPercentage: 40.0,
      activeGoalsCount: 1,
      completedGoalsCount: 0,
    });

    goalService.getGoals.mockResolvedValueOnce([
      {
        id: 'g-1',
        title: 'Emergency Fund',
        description: '6 months buffer',
        targetAmount: 10000.0,
        currentAmount: 4000.0,
        progressPercentage: 40.0,
        targetDate: '2027-12-31',
        daysRemaining: 420,
        status: 'IN_PROGRESS',
      },
    ]);

    renderGoals();

    await waitFor(() => {
      expect(screen.getByText('Financial Goals')).toBeInTheDocument();
      expect(screen.getByText('Emergency Fund')).toBeInTheDocument();
    });

    expect(screen.getByText('Total Target Amount')).toBeInTheDocument();
    expect(screen.getByText('Total Saved Amount')).toBeInTheDocument();
  });

  it('renders empty state when no goals exist', async () => {
    goalService.getGoalSummary.mockResolvedValueOnce({
      totalTargetAmount: 0,
      totalSavedAmount: 0,
      overallProgressPercentage: 0,
      activeGoalsCount: 0,
      completedGoalsCount: 0,
    });
    goalService.getGoals.mockResolvedValueOnce([]);

    renderGoals();

    await waitFor(() => {
      expect(screen.getByText('No Financial Goals Set')).toBeInTheDocument();
    });
  });
});
