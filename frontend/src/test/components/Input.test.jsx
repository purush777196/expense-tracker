import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { Input } from '../../components/common/Input';

describe('Input Component', () => {
  it('renders input with label and placeholder', () => {
    render(
      <Input
        label="Email Address"
        name="email"
        placeholder="Enter your email"
      />
    );
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/enter your email/i)).toBeInTheDocument();
  });

  it('updates value on change', () => {
    const handleChange = vi.fn();
    render(
      <Input
        label="Full Name"
        name="fullName"
        onChange={handleChange}
      />
    );
    const input = screen.getByLabelText(/full name/i);
    fireEvent.change(input, { target: { value: 'John Doe' } });
    expect(handleChange).toHaveBeenCalled();
  });

  it('displays error message and sets aria-invalid when error is present', () => {
    render(
      <Input
        label="Password"
        name="password"
        error="Password is required"
      />
    );
    const input = screen.getByLabelText(/password/i);
    expect(input).toHaveAttribute('aria-invalid', 'true');
    expect(screen.getByRole('alert')).toHaveTextContent('Password is required');
  });

  it('displays helper text when no error is present', () => {
    render(
      <Input
        label="Username"
        name="username"
        helperText="Must be 3-20 characters"
      />
    );
    expect(screen.getByText('Must be 3-20 characters')).toBeInTheDocument();
  });

  it('disables input when disabled prop is set', () => {
    render(
      <Input
        label="Disabled Input"
        name="disabled"
        disabled
      />
    );
    expect(screen.getByLabelText(/disabled input/i)).toBeDisabled();
  });
});

