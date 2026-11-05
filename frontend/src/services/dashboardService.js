import api from './api';

export const dashboardService = {
  async getDashboardSummary(year, month) {
    const params = {};
    if (year) params.year = year;
    if (month) params.month = month;
    return api.get('/dashboard/summary', { params });
  },

  async getCategoryBreakdown(year, month) {
    const params = {};
    if (year) params.year = year;
    if (month) params.month = month;
    return api.get('/dashboard/category-breakdown', { params });
  },

  async getSpendingTrend(months = 6) {
    return api.get('/dashboard/spending-trend', { params: { months } });
  },

  async getRecentTransactions(limit = 5) {
    return api.get('/dashboard/recent-transactions', { params: { limit } });
  },
};

export default dashboardService;
