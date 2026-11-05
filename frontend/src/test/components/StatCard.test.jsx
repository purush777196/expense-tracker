import React from 'react';
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TrendingUp } from 'lucide-react';
import { StatCard } from '../../components/common/StatCard';

describe('StatCard Component', () => {
  it('renders title, value, and subtitle', () => {
    render(
      <StatCard
        title="Total Revenue"
        value="$12,500.00"
        subtitle="Current month"
        icon={TrendingUp}
      />
    );

    expect(screen.getByText('Total Revenue')).toBeInTheDocument();
    expect(screen.getByText('$12,500.00')).toBeInTheDocument();
    expect(screen.getByText('Current month')).toBeInTheDocument();
  });

  it('renders trend information correctly', () => {
    render(
      <StatCard
        title="Net Profit"
        value="$5,000.00"
        trend={{ direction: 'up', label: '+12% vs last month' }}
      />
    );

    expect(screen.getByText('+12% vs last month')).toBeInTheDocument();
  });
});

