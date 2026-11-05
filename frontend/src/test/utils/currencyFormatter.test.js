import { describe, it, expect } from 'vitest';
import {
  formatCurrency,
  getCurrencySymbol,
  CURRENCY_SYMBOLS,
} from '../../utils/currencyFormatter';

describe('currencyFormatter', () => {
  it('formats positive numbers in USD correctly', () => {
    const formatted = formatCurrency(1250.5, 'USD');
    expect(formatted).toContain('1,250.50');
    expect(formatted).toContain('$');
  });

  it('formats zero correctly', () => {
    const formatted = formatCurrency(0, 'USD');
    expect(formatted).toContain('0.00');
  });

  it('handles negative amounts', () => {
    const formatted = formatCurrency(-450.75, 'USD');
    expect(formatted).toContain('450.75');
  });

  it('handles other supported currencies like EUR, GBP, and INR', () => {
    const eur = formatCurrency(100, 'EUR');
    expect(eur).toBeDefined();

    const gbp = formatCurrency(100, 'GBP');
    expect(gbp).toContain('£');

    const inr = formatCurrency(100, 'INR');
    expect(inr).toBeDefined();
  });

  it('handles null, undefined, or invalid inputs gracefully', () => {
    expect(formatCurrency(null)).toContain('0.00');
    expect(formatCurrency(undefined)).toContain('0.00');
    expect(formatCurrency('invalid-number')).toContain('0.00');
  });

  it('returns correct currency symbol', () => {
    expect(CURRENCY_SYMBOLS.USD).toBe('$');
    expect(getCurrencySymbol('USD')).toBe('$');
    expect(getCurrencySymbol('EUR')).toBe('€');
    expect(getCurrencySymbol('GBP')).toBe('£');
    expect(getCurrencySymbol('INR')).toBe('₹');
    expect(getCurrencySymbol('UNKNOWN')).toBe('UNKNOWN');
  });
});

