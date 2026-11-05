import '@testing-library/jest-dom';
import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';

// Automatically cleanup after each test
afterEach(() => {
  cleanup();
  sessionStorage.clear();
  localStorage.clear();
});

// Mock ResizeObserver for Recharts / ResponsiveContainer in JSDOM
global.ResizeObserver = class ResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
};

