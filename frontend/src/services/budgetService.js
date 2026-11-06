import api from './api';

export const budgetService = {
  async getBudgets(params = {}) {
    return api.get('/budgets', { params });
  },

  async getBudgetOverview() {
    return api.get('/budgets/overview');
  },

  async getBudgetById(id) {
    return api.get(`/budgets/${id}`);
  },

  async getBudgetProgress(id) {
    return api.get(`/budgets/${id}/progress`);
  },

  async createBudget(budgetData) {
    return api.post('/budgets', budgetData);
  },

  async updateBudget(id, budgetData) {
    return api.put(`/budgets/${id}`, budgetData);
  },

  async deleteBudget(id) {
    return api.delete(`/budgets/${id}`);
  },

  async getAlerts(unreadOnly = false) {
    return api.get('/budgets/alerts', { params: { unreadOnly } });
  },

  async markAlertAsRead(alertId) {
    return api.patch(`/budgets/alerts/${alertId}/read`);
  },
};

export default budgetService;
