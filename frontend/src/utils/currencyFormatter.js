/**
 * Currency formatting utilities.
 */

export const CURRENCY_SYMBOLS = {
  USD: '$',
  EUR: '€',
  GBP: '£',
  INR: '₹',
  CAD: '$',
  AUD: '$',
  JPY: '¥',
};

export function getCurrencySymbol(currency = 'USD') {
  if (!currency || typeof currency !== 'string') return '$';
  const code = currency.toUpperCase();
  return CURRENCY_SYMBOLS[code] || code;
}

export function formatCurrency(amount, currency = 'USD') {
  if (amount === undefined || amount === null || isNaN(amount)) {
    return '$0.00';
  }

  const validCurrency =
    typeof currency === 'string' && currency.length === 3
      ? currency.toUpperCase()
      : 'USD';

  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: validCurrency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    return `${validCurrency} ${Number(amount).toFixed(2)}`;
  }
}
