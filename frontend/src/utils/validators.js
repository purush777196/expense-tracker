/**
 * Form and input validation helper functions.
 */

export function isValidEmail(email) {
  if (!email || typeof email !== 'string') return false;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email.trim());
}

export function checkPasswordStrength(password) {
  if (!password || typeof password !== 'string') {
    return {
      isValid: false,
      hasMinLength: false,
      hasUppercase: false,
      hasLowercase: false,
      hasDigit: false,
      hasSpecialChar: false,
      score: 0,
    };
  }

  const hasMinLength = password.length >= 8;
  const hasUppercase = /[A-Z]/.test(password);
  const hasLowercase = /[a-z]/.test(password);
  const hasDigit = /\d/.test(password);
  const hasSpecialChar = /[@$!%*?&#^()_+\-={}[\]|;:'",.<>/]/.test(password);

  const criteria = [hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSpecialChar];
  const score = criteria.filter(Boolean).length;
  const isValid = score === 5;

  return {
    isValid,
    hasMinLength,
    hasUppercase,
    hasLowercase,
    hasDigit,
    hasSpecialChar,
    score,
  };
}

export function validatePassword(password) {
  const result = checkPasswordStrength(password);
  if (!password || typeof password !== 'string') {
    return { isValid: false, message: 'Password is required' };
  }
  if (!result.hasMinLength) {
    return { isValid: false, message: 'Password must be at least 8 characters long' };
  }
  if (!result.isValid) {
    return {
      isValid: false,
      message: 'Password must include uppercase, lowercase, number, and special character',
    };
  }
  return { isValid: true, message: '' };
}

export function isPositiveNumber(value) {
  if (value === null || value === undefined || value === '') return false;
  const num = Number(value);
  return !isNaN(num) && num > 0;
}

export function hasLength(value, min = 1, max = Infinity) {
  if (typeof value !== 'string') return false;
  const len = value.trim().length;
  return len >= min && len <= max;
}
