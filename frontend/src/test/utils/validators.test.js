import { describe, it, expect } from 'vitest';
import {
  isValidEmail,
  checkPasswordStrength,
  isPositiveNumber,
  hasLength,
} from '../../utils/validators';

describe('validators', () => {
  describe('isValidEmail', () => {
    it('returns true for valid email formats', () => {
      expect(isValidEmail('user@example.com')).toBe(true);
      expect(isValidEmail('first.last@sub.domain.org')).toBe(true);
      expect(isValidEmail('user+tag@domain.co.uk')).toBe(true);
    });

    it('returns false for invalid email formats', () => {
      expect(isValidEmail('')).toBe(false);
      expect(isValidEmail('plainaddress')).toBe(false);
      expect(isValidEmail('@missingusername.com')).toBe(false);
      expect(isValidEmail('missingdomain@.com')).toBe(false);
      expect(isValidEmail('user@domain')).toBe(false);
      expect(isValidEmail(null)).toBe(false);
    });
  });

  describe('checkPasswordStrength', () => {
    it('verifies all password complexity rules for valid password', () => {
      const result = checkPasswordStrength('Password123!');
      expect(result.isValid).toBe(true);
      expect(result.hasMinLength).toBe(true);
      expect(result.hasUppercase).toBe(true);
      expect(result.hasLowercase).toBe(true);
      expect(result.hasDigit).toBe(true);
      expect(result.hasSpecialChar).toBe(true);
      expect(result.score).toBe(5);
    });

    it('detects short passwords', () => {
      const result = checkPasswordStrength('Pass1!');
      expect(result.hasMinLength).toBe(false);
      expect(result.isValid).toBe(false);
    });

    it('detects missing uppercase', () => {
      const result = checkPasswordStrength('password123!');
      expect(result.hasUppercase).toBe(false);
      expect(result.isValid).toBe(false);
    });

    it('detects missing lowercase', () => {
      const result = checkPasswordStrength('PASSWORD123!');
      expect(result.hasLowercase).toBe(false);
      expect(result.isValid).toBe(false);
    });

    it('detects missing digit', () => {
      const result = checkPasswordStrength('Password!!!!');
      expect(result.hasDigit).toBe(false);
      expect(result.isValid).toBe(false);
    });

    it('detects missing special character', () => {
      const result = checkPasswordStrength('Password123');
      expect(result.hasSpecialChar).toBe(false);
      expect(result.isValid).toBe(false);
    });

    it('handles empty or null values', () => {
      const result = checkPasswordStrength('');
      expect(result.isValid).toBe(false);
      expect(result.score).toBe(0);
    });
  });

  describe('isPositiveNumber', () => {
    it('validates positive numbers correctly', () => {
      expect(isPositiveNumber(100)).toBe(true);
      expect(isPositiveNumber('50.25')).toBe(true);
      expect(isPositiveNumber(0)).toBe(false);
      expect(isPositiveNumber(-10)).toBe(false);
      expect(isPositiveNumber('abc')).toBe(false);
    });
  });

  describe('hasLength', () => {
    it('validates string lengths within bounds', () => {
      expect(hasLength('hello', 2, 10)).toBe(true);
      expect(hasLength('hi', 3)).toBe(false);
      expect(hasLength('too long string', 1, 5)).toBe(false);
    });
  });
});

