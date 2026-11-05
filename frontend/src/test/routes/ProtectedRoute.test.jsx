import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '../../routes/ProtectedRoute';
import * as authHook from '../../hooks/useAuth';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

describe('ProtectedRoute', () => {
  it('renders loading indicator when auth is loading', () => {
    authHook.useAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: true,
    });

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<div>Dashboard Content</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText(/verifying secure session/i)).toBeInTheDocument();
    expect(screen.queryByText('Dashboard Content')).not.toBeInTheDocument();
  });

  it('redirects unauthenticated user to /login', () => {
    authHook.useAuth.mockReturnValue({
      isAuthenticated: false,
      isLoading: false,
    });

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route path="/login" element={<div>Login Page Mock</div>} />
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<div>Dashboard Content</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Login Page Mock')).toBeInTheDocument();
    expect(screen.queryByText('Dashboard Content')).not.toBeInTheDocument();
  });

  it('renders protected child component when user is authenticated', () => {
    authHook.useAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      user: { fullName: 'John Doe' },
    });

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<div>Dashboard Content</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Dashboard Content')).toBeInTheDocument();
  });
});

