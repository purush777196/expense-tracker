import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import { AuthProvider } from '../../context/AuthContext';
import { useAuth } from '../../hooks/useAuth';

const { mockLogin, mockLogout, mockRefreshToken } = vi.hoisted(() => ({
  mockLogin: vi.fn(),
  mockLogout: vi.fn(),
  mockRefreshToken: vi.fn(),
}));

vi.mock('../../services/authService', () => ({
  default: {
    login: mockLogin,
    logout: mockLogout,
    refreshToken: mockRefreshToken,
    getCurrentUser: vi.fn(),
    register: vi.fn(),
  },
  authService: {
    login: mockLogin,
    logout: mockLogout,
    refreshToken: mockRefreshToken,
    getCurrentUser: vi.fn(),
    register: vi.fn(),
  },
}));

const TestConsumer = () => {
  const { user, isAuthenticated, isLoading, login, logout } = useAuth();

  return (
    <div>
      <div data-testid="loading">{isLoading ? 'loading' : 'ready'}</div>
      <div data-testid="auth">{isAuthenticated ? 'authenticated' : 'unauthenticated'}</div>
      <div data-testid="user">{user?.fullName || 'no-user'}</div>
      <button
        onClick={() =>
          login({ email: 'test@example.com', password: 'Password123!' })
        }
      >
        Trigger Login
      </button>
      <button onClick={() => logout()}>Trigger Logout</button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    sessionStorage.clear();
  });

  it('initializes in unauthenticated state when no session exists', async () => {
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth')).toHaveTextContent('unauthenticated');
    expect(screen.getByTestId('user')).toHaveTextContent('no-user');
  });

  it('handles login and updates authenticated user state', async () => {
    const mockUser = { id: 'u1', fullName: 'Jane Doe', email: 'jane@example.com' };
    mockLogin.mockResolvedValueOnce({
      accessToken: 'sample-access-token',
      refreshToken: 'sample-refresh-token',
      user: mockUser,
    });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Trigger Login').click();
    });

    expect(screen.getByTestId('auth')).toHaveTextContent('authenticated');
    expect(screen.getByTestId('user')).toHaveTextContent('Jane Doe');
    expect(sessionStorage.getItem('expense_tracker_refresh_token')).toBe('sample-refresh-token');
  });

  it('handles logout and clears user state', async () => {
    const mockUser = { id: 'u1', fullName: 'Jane Doe', email: 'jane@example.com' };
    mockLogin.mockResolvedValueOnce({
      accessToken: 'sample-access-token',
      refreshToken: 'sample-refresh-token',
      user: mockUser,
    });

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await act(async () => {
      screen.getByText('Trigger Login').click();
    });

    expect(screen.getByTestId('auth')).toHaveTextContent('authenticated');

    await act(async () => {
      screen.getByText('Trigger Logout').click();
    });

    expect(screen.getByTestId('auth')).toHaveTextContent('unauthenticated');
    expect(screen.getByTestId('user')).toHaveTextContent('no-user');
    expect(sessionStorage.getItem('expense_tracker_refresh_token')).toBeNull();
  });
});
