import { describe, it, expect } from 'vitest';
import {
  formatDate,
  formatDateTime,
  formatMonthYear,
  toIsoDateString,
} from '../../utils/dateFormatter';

describe('dateFormatter', () => {
  it('formats dates consistently', () => {
    const formatted = formatDate('2026-11-05T10:00:00Z');
    expect(formatted).toContain('Nov');
    expect(formatted).toContain('2026');
  });

  it('formats month and year', () => {
    const formatted = formatMonthYear('2026-11-05T10:00:00Z');
    expect(formatted).toContain('Nov');
    expect(formatted).toContain('2026');
  });

  it('formats ISO date string (YYYY-MM-DD)', () => {
    const date = new Date(2026, 10, 5); // month 10 is November (0-indexed)
    const isoDate = toIsoDateString(date);
    expect(isoDate).toBe('2026-11-05');
  });

  it('handles empty or invalid date gracefully', () => {
    expect(formatDate(null)).toBe('-');
    expect(formatDate(undefined)).toBe('-');
    expect(formatDate('invalid-date')).toBe('-');
    expect(formatDateTime(null)).toBe('-');
    expect(formatMonthYear(null)).toBe('-');
    expect(toIsoDateString(null)).toBe('');
  });
});

